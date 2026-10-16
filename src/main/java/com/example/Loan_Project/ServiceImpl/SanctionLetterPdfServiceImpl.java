
package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.Entity.LoanApplication;
import com.example.Loan_Project.Entity.LoanProcessing;
import com.example.Loan_Project.Service.SanctionLetterPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class SanctionLetterPdfServiceImpl
        implements SanctionLetterPdfService {

    @Value("${app.documents.sanction-letter-dir:uploads/sanction-letters}")
    private String outputDirectory;

    private static final PDFont REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDFont BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    // Professional document color palette
    private static final Color NAVY = new Color(26, 47, 79);
    private static final Color BLUE = new Color(47, 104, 173);
    private static final Color LIGHT_BLUE = new Color(235, 243, 252);
    private static final Color TEXT = new Color(43, 53, 68);
    private static final Color MUTED = new Color(105, 116, 132);
    private static final Color BORDER = new Color(218, 225, 235);
    private static final Color LIGHT_BACKGROUND =
            new Color(248, 250, 253);
    private static final Color WHITE = Color.WHITE;

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 48;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Override
    public Path generate(
            LoanApplication application,
            LoanProcessing processing) throws IOException {

        Path directory = Paths.get(outputDirectory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(directory);

        String fileName = processing.getSanctionLetterNumber()
                .replaceAll("[^A-Za-z0-9_-]", "") + ".pdf";

        Path pdfPath = directory.resolve(fileName).normalize();

        if (!pdfPath.startsWith(directory)) {
            throw new IOException("Invalid sanction letter file path");
        }

        String customerName = getCustomerName(application);
        String loanType = application.getLoanDeal() == null
                || application.getLoanDeal().getLoanType() == null
                ? "N/A"
                : application.getLoanDeal().getLoanType()
                .name().replace('_', ' ');

        LocalDateTime issueDate = LocalDateTime.now();

        try (PDDocument document = new PDDocument()) {

            document.getDocumentInformation()
                    .setTitle("Loan Sanction Letter - "
                            + processing.getSanctionLetterNumber());

            document.getDocumentInformation()
                    .setAuthor("Loan Project");

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                // -------------------------------------------------
                // 1. Header
                // -------------------------------------------------
                fillRect(content, 0, PAGE_HEIGHT - 110,
                        PAGE_WIDTH, 110, NAVY);

                fillRect(content, 0, PAGE_HEIGHT - 114,
                        PAGE_WIDTH, 4, BLUE);

                addText(content, "LOAN PROJECT",
                        MARGIN, 799, 19, true, WHITE);

                addText(content, "LOAN SANCTION LETTER",
                        MARGIN, 775, 10, false,
                        new Color(211, 225, 244));

                // Status label in the upper-right corner
                float badgeWidth = 112;
                float badgeHeight = 28;
                float badgeX = PAGE_WIDTH - MARGIN - badgeWidth;
                float badgeY = 772;

                fillRect(content, badgeX, badgeY,
                        badgeWidth, badgeHeight,
                        new Color(43, 81, 125));

                drawOutlineRect(content, badgeX, badgeY,
                        badgeWidth, badgeHeight,
                        new Color(100, 143, 191), 0.8f);

                addCenteredText(content, "SANCTIONED",
                        badgeX, badgeY + 10,
                        badgeWidth, 9, true, WHITE);

                // -------------------------------------------------
                // 2. Letter reference and issue date
                // -------------------------------------------------
                addText(content, "LETTER NUMBER",
                        MARGIN, 700, 7, true, MUTED);

                addText(content,
                        processing.getSanctionLetterNumber(),
                        MARGIN, 682, 10, true, TEXT);

                float dateColumnX = 350;

                addText(content, "ISSUE DATE",
                        dateColumnX, 700, 7, true, MUTED);

                addText(content,
                        issueDate.format(DATE_FORMAT),
                        dateColumnX, 682, 10, true, TEXT);

                drawLine(content, MARGIN, 665,
                        PAGE_WIDTH - MARGIN, 665, BORDER, 0.8f);

                // -------------------------------------------------
                // 3. Customer greeting
                // -------------------------------------------------
                addText(content, "Dear " + customerName + ",",
                        MARGIN, 643, 11, true, TEXT);

                addText(content,
                        "We are pleased to confirm that your loan has been sanctioned.",
                        MARGIN, 624, 9, false, TEXT);

                // -------------------------------------------------
                // 4. Highlighted sanctioned amount
                // -------------------------------------------------
                float amountBoxY = 550;
                float amountBoxHeight = 56;

                fillRect(content, MARGIN, amountBoxY,
                        CONTENT_WIDTH, amountBoxHeight, LIGHT_BLUE);

                fillRect(content, MARGIN, amountBoxY,
                        5, amountBoxHeight, BLUE);

                drawOutlineRect(content, MARGIN, amountBoxY,
                        CONTENT_WIDTH, amountBoxHeight, BORDER, 0.7f);

                addText(content, "SANCTIONED AMOUNT",
                        MARGIN + 17, amountBoxY + 36,
                        8, true, BLUE);

                addText(content,
                        "INR " + formatAmount(
                                processing.getSanctionedAmount()),
                        MARGIN + 17, amountBoxY + 14,
                        19, true, NAVY);

                // -------------------------------------------------
                // 5. Loan details table
                // -------------------------------------------------
                addText(content, "LOAN DETAILS",
                        MARGIN, 527, 11, true, NAVY);

                float tableTop = 512;
                float rowHeight = 39;
                float halfWidth = CONTENT_WIDTH / 2;

                // Row 1
                drawTableRow(content, tableTop, rowHeight, 0,
                        "APPLICATION ID",
                        String.valueOf(application.getLoanApplicationId()),
                        "LOAN TYPE",
                        loanType);

                // Row 2
                drawTableRow(content, tableTop, rowHeight, 1,
                        "ANNUAL INTEREST RATE",
                        formatRate(processing.getInterestRate()) + "%",
                        "LOAN TENURE",
                        formatTenure(processing.getTenureMonths()));

                // Row 3
                drawTableRow(content, tableTop, rowHeight, 2,
                        "MONTHLY EMI",
                        "INR " + formatAmount(processing.getMonthlyEmi()),
                        "EMI PAYMENT DAY",
                        formatEmiDay(application.getEmiDay()));

                // Table outside border and dividers
                float tableBottom = tableTop - (3 * rowHeight);

                drawOutlineRect(content, MARGIN, tableBottom,
                        CONTENT_WIDTH, 3 * rowHeight, BORDER, 0.8f);

                drawLine(content, MARGIN + halfWidth, tableBottom,
                        MARGIN + halfWidth, tableTop, BORDER, 0.8f);

                drawLine(content, MARGIN, tableTop - rowHeight,
                        PAGE_WIDTH - MARGIN, tableTop - rowHeight,
                        BORDER, 0.6f);

                drawLine(content, MARGIN, tableTop - (2 * rowHeight),
                        PAGE_WIDTH - MARGIN, tableTop - (2 * rowHeight),
                        BORDER, 0.6f);

                // -------------------------------------------------
                // 6. Terms and conditions
                // -------------------------------------------------
                addText(content, "TERMS AND CONDITIONS",
                        MARGIN, 372, 10, true, NAVY);

                addText(content,
                        "1. Disbursement is subject to completion of applicable formalities.",
                        MARGIN, 350, 8.5f, false, TEXT);

                addText(content,
                        "2. EMI payments must follow the agreed repayment schedule.",
                        MARGIN, 334, 8.5f, false, TEXT);

                addText(content,
                        "3. The sanctioned amount, interest rate and tenure stated above",
                        MARGIN, 318, 8.5f, false, TEXT);

                addText(content,
                        "   form part of the approved loan terms.",
                        MARGIN, 305, 8.5f, false, TEXT);

                // -------------------------------------------------
                // 7. Closing and authorization
                // -------------------------------------------------
                addText(content, "Regards,",
                        MARGIN, 263, 9, false, TEXT);

                addText(content, "For Loan Project",
                        MARGIN, 245, 10, true, TEXT);

                float signatureLineY = 207;

                drawLine(content, MARGIN, signatureLineY,
                        MARGIN + 155, signatureLineY, MUTED, 0.8f);

                addText(content, "Authorized Signatory",
                        MARGIN, signatureLineY - 15,
                        8.5f, true, TEXT);

                addText(content, "Loan Project",
                        MARGIN, signatureLineY - 29,
                        8, false, MUTED);

                // -------------------------------------------------
                // 8. Footer
                // -------------------------------------------------
                drawLine(content, MARGIN, 82,
                        PAGE_WIDTH - MARGIN, 82, BORDER, 0.7f);

                addCenteredText(content,
                        "Computer-generated sanction letter.",
                        MARGIN, 63, CONTENT_WIDTH,
                        8, false, MUTED);

                addCenteredText(content,
                        "Please retain this letter for your records.",
                        MARGIN, 49, CONTENT_WIDTH,
                        8, false, MUTED);
            }

            document.save(pdfPath.toFile());
        }

        return pdfPath;
    }

    private void drawTableRow(
            PDPageContentStream content,
            float tableTop,
            float rowHeight,
            int rowIndex,
            String leftLabel,
            String leftValue,
            String rightLabel,
            String rightValue) throws IOException {

        float rowY = tableTop - ((rowIndex + 1) * rowHeight);
        float halfWidth = CONTENT_WIDTH / 2;

        Color background = rowIndex % 2 == 0
                ? WHITE
                : LIGHT_BACKGROUND;

        fillRect(content, MARGIN, rowY,
                CONTENT_WIDTH, rowHeight, background);

        float leftX = MARGIN + 12;
        float rightX = MARGIN + halfWidth + 12;

        addText(content, leftLabel,
                leftX, rowY + 23, 7, true, MUTED);

        addText(content, leftValue,
                leftX, rowY + 8, 9, true, TEXT);

        addText(content, rightLabel,
                rightX, rowY + 23, 7, true, MUTED);

        addText(content, rightValue,
                rightX, rowY + 8, 9, true, TEXT);
    }

    private void fillRect(
            PDPageContentStream content,
            float x,
            float y,
            float width,
            float height,
            Color color) throws IOException {

        content.setNonStrokingColor(color);
        content.addRect(x, y, width, height);
        content.fill();
    }

    private void drawOutlineRect(
            PDPageContentStream content,
            float x,
            float y,
            float width,
            float height,
            Color color,
            float lineWidth) throws IOException {

        content.setStrokingColor(color);
        content.setLineWidth(lineWidth);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawLine(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2,
            Color color,
            float lineWidth) throws IOException {

        content.setStrokingColor(color);
        content.setLineWidth(lineWidth);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private void addText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float fontSize,
            boolean bold,
            Color color) throws IOException {

        String safeText = toPdfSafeText(text);
        PDFont font = bold ? BOLD : REGULAR;

        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(safeText);
        content.endText();
    }

    private void addCenteredText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float width,
            float fontSize,
            boolean bold,
            Color color) throws IOException {

        String safeText = toPdfSafeText(text);
        PDFont font = bold ? BOLD : REGULAR;

        float textWidth = font.getStringWidth(safeText)
                / 1000f * fontSize;

        float textX = x + (width - textWidth) / 2;

        addText(content, safeText, textX, y, fontSize, bold, color);
    }

    private String getCustomerName(LoanApplication application) {

        if (application.getCustomer() == null) {
            return "Customer";
        }

        String firstName = application.getCustomer().getFirstName();
        String lastName = application.getCustomer().getLastName();

        String name = (firstName == null ? "" : firstName.trim())
                + " "
                + (lastName == null ? "" : lastName.trim());

        name = name.trim();

        return name.isBlank() ? "Customer" : toPdfSafeText(name);
    }

    private String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "N/A";
        }

        NumberFormat format =
                NumberFormat.getNumberInstance(
                        Locale.forLanguageTag("en-IN"));

        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        return format.format(amount);
    }

    private String formatRate(BigDecimal rate) {
        return rate == null
                ? "N/A"
                : rate.setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String formatTenure(Integer tenureMonths) {
        return tenureMonths == null
                ? "N/A"
                : tenureMonths + " months";
    }

    private String formatEmiDay(Integer emiDay) {

        if (emiDay == null || emiDay < 1 || emiDay > 31) {
            return "Not specified";
        }

        String suffix;

        if (emiDay >= 11 && emiDay <= 13) {
            suffix = "th";
        } else {
            suffix = switch (emiDay % 10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }

        return emiDay + suffix + " of each month";
    }

    private String toPdfSafeText(String value) {

        return value == null
                ? ""
                : value.replaceAll("[^\\x20-\\x7E]", " ");
    }
}
