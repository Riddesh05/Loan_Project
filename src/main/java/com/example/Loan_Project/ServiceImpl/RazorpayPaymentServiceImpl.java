package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.*;
import com.example.Loan_Project.Entity.EmiSchedule;
import com.example.Loan_Project.Exception.BusinessException;
import com.example.Loan_Project.Exception.ResourceNotFoundException;
import com.example.Loan_Project.Repository.EmiScheduleRepository;
import com.example.Loan_Project.Service.EmiScheduleService;
import com.example.Loan_Project.Service.RazorpayPaymentService;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentServiceImpl
        implements RazorpayPaymentService {

    private final RazorpayClient razorpayClient;
    private final EmiScheduleService emiScheduleService;
    private final EmiScheduleRepository emiScheduleRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    @Transactional
    public RazorpayCreateOrderResponse createOrder(
            RazorpayCreateOrderRequest request) {

        EmiSchedule schedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "EMI schedule not found"));

        Long loanAccountId = schedule.getLoanAccount()
                .getLoanAccountId();

        // Server-side breakdown is the source of the payable amount.
        PaymentBreakdownResponse breakdown =
                emiScheduleService.getPaymentBreakdown(
                        request.getEmiScheduleId());

        if (!loanAccountId.equals(breakdown.getLoanAccountId())) {
            throw new BusinessException(
                    "Loan account and EMI schedule mismatch");
        }

        BigDecimal payable = breakdown.getTotalPayable();

        if (payable == null
                || payable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Payment amount must be greater than zero");
        }

        // Razorpay requires INR in paise: ₹100 = 10000 paise.
        long amountPaise = payable
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        try {
            JSONObject notes = new JSONObject();
            notes.put("loanAccountId", loanAccountId.toString());
            notes.put("emiScheduleId",
                    request.getEmiScheduleId().toString());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put(
                    "receipt",
                    "EMI-" + request.getEmiScheduleId()
                            + "-" + System.currentTimeMillis());
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            return new RazorpayCreateOrderResponse(
                    order.get("id"),
                    order.get("currency"),
                    ((Number) order.get("amount")).longValue(),
                    keyId,
                    request.getEmiScheduleId(),
                    loanAccountId
            );

        } catch (RazorpayException ex) {
            throw new BusinessException(
                    "Unable to create Razorpay order: "
                            + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void verifyPayment(RazorpayVerifyRequest request) {

        EmiSchedule schedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "EMI schedule not found"));

        Long actualLoanAccountId = schedule.getLoanAccount()
                .getLoanAccountId();

        if (!actualLoanAccountId.equals(request.getLoanAccountId())) {
            throw new BusinessException(
                    "EMI does not belong to the given loan account");
        }

        try {
            // 1. Verify checkout signature using the server secret.
            JSONObject signatureAttributes = new JSONObject();
            signatureAttributes.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId());
            signatureAttributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId());
            signatureAttributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature());

            boolean signatureValid;

            try {
                Utils.verifyPaymentSignature(
                        signatureAttributes, keySecret);
                signatureValid = true;
            } catch (RazorpayException | RuntimeException ex) {
                signatureValid = false;
            }

            if (!signatureValid) {
                throw new BusinessException(
                        "Invalid Razorpay payment signature");
            }

            // 2. Fetch the order directly from Razorpay.
            Order order = razorpayClient.orders.fetch(
                    request.getRazorpayOrderId());

            // 3. Fetch payment directly from Razorpay.
            Payment payment = razorpayClient.payments.fetch(
                    request.getRazorpayPaymentId());

            String paymentOrderId = payment.has("order_id")
                    ? payment.get("order_id") : null;

            if (!request.getRazorpayOrderId().equals(paymentOrderId)) {
                throw new BusinessException(
                        "Payment does not belong to this Razorpay order");
            }

            // 4. Check order metadata against the requested EMI.
            JSONObject notes = order.has("notes")
                    && !order.toJson().isNull("notes")
                    ? order.toJson().getJSONObject("notes")
                    : new JSONObject();

            if (!request.getLoanAccountId().toString().equals(
                    notes.optString("loanAccountId"))
                    || !request.getEmiScheduleId().toString().equals(
                    notes.optString("emiScheduleId"))) {
                throw new BusinessException(
                        "Razorpay order does not match this EMI");
            }

            // 5. Recalculate expected amount on the server.
            PaymentBreakdownResponse breakdown =
                    emiScheduleService.getPaymentBreakdown(
                            request.getEmiScheduleId());

            long expectedPaise = breakdown.getTotalPayable()
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();

            long orderAmount =
                    ((Number) order.get("amount")).longValue();

            long paymentAmount =
                    ((Number) payment.get("amount")).longValue();

            if (orderAmount != expectedPaise
                    || paymentAmount != expectedPaise) {
                throw new BusinessException(
                        "Razorpay amount does not match the payable EMI amount");
            }

            // 6. Require captured payment before EMI settlement.
            String status = payment.get("status");

            if (!"captured".equalsIgnoreCase(status)) {
                throw new BusinessException(
                        "Payment is not captured yet. Current status: "
                                + status);
            }

            if (payment.has("currency")
                    && !"INR".equalsIgnoreCase(
                    payment.get("currency"))) {
                throw new BusinessException(
                        "Payment currency must be INR");
            }

            // 7. Reuse existing settlement logic only after
            // provider-side verification succeeds.
            PaymentVerifyRequest settlement =
                    new PaymentVerifyRequest();

            settlement.setLoanAccountId(
                    request.getLoanAccountId());
            settlement.setEmiScheduleId(
                    request.getEmiScheduleId());
            settlement.setAmountPaid(
                    breakdown.getTotalPayable());
            settlement.setTransactionRefNo(
                    request.getRazorpayPaymentId());
            settlement.setPaymentMode("RAZORPAY");

            emiScheduleService.verifyPayment(settlement);

        } catch (RazorpayException ex) {
            throw new BusinessException(
                    "Unable to verify payment with Razorpay: "
                            + ex.getMessage());
        }
    }



    @Override
    public Map<String, Object> getOrderDetails(String orderId) {
        try {
            Order order = razorpayClient.orders.fetch(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("status", order.get("status"));
            response.put("attempts", order.get("attempts"));

            return response;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch Razorpay order: " + e.getMessage()
            );
        }
    }
}