package in.sfp.main.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import in.sfp.main.dto.PaperGenerationRequest;
import in.sfp.main.dto.QuestionRequest;
import in.sfp.main.dto.SectionRequest;
import in.sfp.main.models.Questions;
import in.sfp.main.models.Organisation;
import in.sfp.main.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class PdfExportService {

    @Autowired
    private OrganisationRepository organisationRepository;

    public byte[] generateQuestionPaper(QuestionRequest request) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("QuestBank Export", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            if (request.getOrganisation() != null) {
                Paragraph org = new Paragraph(request.getOrganisation(),
                        FontFactory.getFont(FontFactory.HELVETICA, 12));
                org.setAlignment(Element.ALIGN_CENTER);
                document.add(org);
            }

            document.add(new Paragraph("\n"));

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);

            List<Questions> questions = request.getQuestions();
            if (questions != null) {
                for (int i = 0; i < questions.size(); i++) {
                    Questions q = questions.get(i);
                    Paragraph qText = new Paragraph((i + 1) + ". " + q.getQuestion(), normalFont);
                    qText.setSpacingBefore(10);
                    document.add(qText);

                    if (request.getQuestionType() == in.sfp.main.enums.QuestionType.MCQ) {
                        if (q.getOption1() != null)
                            document.add(new Paragraph("   A) " + q.getOption1(), normalFont));
                        if (q.getOption2() != null)
                            document.add(new Paragraph("   B) " + q.getOption2(), normalFont));
                        if (q.getOption3() != null)
                            document.add(new Paragraph("   C) " + q.getOption3(), normalFont));
                        if (q.getOption4() != null)
                            document.add(new Paragraph("   D) " + q.getOption4(), normalFont));
                    }
                    document.add(new Paragraph(" ", italicFont));
                }
            }
            Paragraph endPara = new Paragraph("\n\n--- End of Question Paper ---", italicFont);
            endPara.setAlignment(Element.ALIGN_CENTER);
            document.add(endPara);
        } finally {
            if (document.isOpen())
                document.close();
        }
        return out.toByteArray();
    }

    public byte[] generateMultiSectionPaper(PaperGenerationRequest request, List<List<Questions>> sectionQuestions)
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            int totalQ = 0;
            int totalMarks = 0;
            for (int i = 0; i < request.getSections().size(); i++) {
                int qInSec = sectionQuestions.get(i).size();
                totalQ += qInSec;
                totalMarks += (qInSec * request.getSections().get(i).getMarksPerQuestion());
            }

            addCustomHeader(document, request.getOrganisation(), request.getExamType(), request.getTotalTime(),
                    request.getSetNumber(), totalQ, totalMarks, request.getSections());

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            for (int s = 0; s < request.getSections().size(); s++) {
                SectionRequest sr = request.getSections().get(s);
                List<Questions> questions = sectionQuestions.get(s);

                // Section Header with Centered Title and Right-Aligned Marks
                PdfPTable sectionHeaderTable = new PdfPTable(3);
                sectionHeaderTable.setWidthPercentage(100);
                sectionHeaderTable.setWidths(new float[] { 1, 2, 1 });

                PdfPCell emptyLeft = new PdfPCell(new Phrase(""));
                emptyLeft.setBorder(Rectangle.NO_BORDER);
                sectionHeaderTable.addCell(emptyLeft);

                PdfPCell titleCell = new PdfPCell(new Phrase(sr.getSectionName().toUpperCase(), sectionFont));
                titleCell.setBorder(Rectangle.NO_BORDER);
                titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sectionHeaderTable.addCell(titleCell);

                PdfPCell marksCell = new PdfPCell(
                        new Phrase("(" + (questions.size() * sr.getMarksPerQuestion()) + " Marks)", normalFont));
                marksCell.setBorder(Rectangle.NO_BORDER);
                marksCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                sectionHeaderTable.addCell(marksCell);

                sectionHeaderTable.setSpacingBefore(15);
                document.add(sectionHeaderTable);

                for (int i = 0; i < questions.size(); i++) {
                    Questions q = questions.get(i);
                    Paragraph qText = new Paragraph((i + 1) + ". " + q.getQuestion(), normalFont);
                    qText.setSpacingBefore(8);
                    document.add(qText);

                    if (q.getQuestionType() == in.sfp.main.enums.QuestionType.MCQ) {
                        if (q.getOption1() != null)
                            document.add(new Paragraph("   A) " + q.getOption1(), normalFont));
                        if (q.getOption2() != null)
                            document.add(new Paragraph("   B) " + q.getOption2(), normalFont));
                        if (q.getOption3() != null)
                            document.add(new Paragraph("   C) " + q.getOption3(), normalFont));
                        if (q.getOption4() != null)
                            document.add(new Paragraph("   D) " + q.getOption4(), normalFont));
                    }
                }
            }
            Paragraph endPara = new Paragraph("\n\n--- End of Question Paper ---",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            endPara.setAlignment(Element.ALIGN_CENTER);
            document.add(endPara);
        } finally {
            if (document.isOpen())
                document.close();
        }
        return out.toByteArray();
    }

    private void addCustomHeader(Document document, String orgName, String examType, String totalTime,
            String setNumber, int totalQuestions, int totalMarks, List<SectionRequest> sections) throws Exception {
        Font boldHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font midFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        // 1st Layer: Logo and Organisation Name/Exam Info
        com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 1, 3, 1 });

        // Logo
        com.lowagie.text.pdf.PdfPCell logoCell = new com.lowagie.text.pdf.PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        Optional<Organisation> orgOpt = organisationRepository.findById(orgName != null ? orgName : "Unknown");
        if (orgOpt.isPresent() && orgOpt.get().getLogo() != null) {
            Image logo = Image.getInstance(orgOpt.get().getLogo());
            logo.scaleToFit(60, 60);
            logoCell.addElement(logo);
        }
        headerTable.addCell(logoCell);

        // Center Content
        com.lowagie.text.pdf.PdfPCell centerCell = new com.lowagie.text.pdf.PdfPCell();
        centerCell.setBorder(Rectangle.NO_BORDER);
        centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pOrg = new Paragraph(orgName != null ? orgName.toUpperCase() : "ORGANISATION NAME", boldHeader);
        pOrg.setAlignment(Element.ALIGN_CENTER);
        centerCell.addElement(pOrg);

        Paragraph pExam = new Paragraph(examType != null ? examType : "ANNUAL EXAMINATION", midFont);
        pExam.setAlignment(Element.ALIGN_CENTER);
        centerCell.addElement(pExam);

        // Session Calculation
        java.time.LocalDate now = java.time.LocalDate.now();
        int year = now.getYear();
        String session = (now.getMonthValue() >= 4) ? year + "-" + (year + 1) : (year - 1) + "-" + year;
        Paragraph pSession = new Paragraph("SESSION: " + session, midFont);
        pSession.setAlignment(Element.ALIGN_CENTER);
        centerCell.addElement(pSession);

        headerTable.addCell(centerCell);

        // Right Space
        com.lowagie.text.pdf.PdfPCell rightCell = new com.lowagie.text.pdf.PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(rightCell);

        document.add(headerTable);

        // Name and Roll Number Fields Table
        PdfPTable nameRollTable = new PdfPTable(2);
        nameRollTable.setWidthPercentage(100);
        nameRollTable.setSpacingBefore(10);

        PdfPCell nameCell = new PdfPCell(new Phrase("Name: __________________________", smallBold));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameRollTable.addCell(nameCell);

        PdfPCell rollCell = new PdfPCell(new Phrase("Roll No: ____________", smallBold));
        rollCell.setBorder(Rectangle.NO_BORDER);
        rollCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        nameRollTable.addCell(rollCell);

        document.add(nameRollTable);

        // Time and Marks Column
        com.lowagie.text.pdf.PdfPTable metaTable = new com.lowagie.text.pdf.PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setSpacingBefore(10);

        // Add Set Number Row
        com.lowagie.text.pdf.PdfPCell emptyCell = new com.lowagie.text.pdf.PdfPCell(new Phrase(""));
        emptyCell.setBorder(Rectangle.NO_BORDER);
        metaTable.addCell(emptyCell);

        com.lowagie.text.pdf.PdfPCell setCell = new com.lowagie.text.pdf.PdfPCell(
                new Phrase("SET: " + (setNumber != null && !setNumber.isEmpty() ? setNumber : "1"), smallBold));
        setCell.setBorder(Rectangle.NO_BORDER);
        setCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaTable.addCell(setCell);

        com.lowagie.text.pdf.PdfPCell timeCell = new com.lowagie.text.pdf.PdfPCell(
                new Phrase("Total Time: " + (totalTime != null ? totalTime : "3 Hours"), smallBold));
        timeCell.setBorder(Rectangle.NO_BORDER);
        metaTable.addCell(timeCell);

        com.lowagie.text.pdf.PdfPCell marksCell = new com.lowagie.text.pdf.PdfPCell(
                new Phrase("Total Marks: " + totalMarks, smallBold));
        marksCell.setBorder(Rectangle.NO_BORDER);
        marksCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaTable.addCell(marksCell);

        document.add(metaTable);
        document.add(new Paragraph("______________________________________________________________________________",
                normalFont));

        // Instructions
        Paragraph instHeader = new Paragraph("General Instructions:", midFont);
        instHeader.setSpacingBefore(10);
        document.add(instHeader);

        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.ORDERED);
        list.setListSymbol(new Chunk("")); // Empty so it uses numeric 1, 2, 3
        list.add(new ListItem("Read the question paper carefully before you start writing.", normalFont));
        list.add(new ListItem("Write your name and the date on the question paper and the answer sheet.", normalFont));
        list.add(new ListItem("Write only the question numbers followed by the answers.", normalFont));
        list.add(new ListItem(
                "This question paper contains " + totalQuestions + " questions and All questions are compulsory.",
                normalFont));

        if (sections != null && !sections.isEmpty()) {
            list.add(
                    new ListItem("This questions paper is divided into " + sections.size() + " sections.", normalFont));
            for (SectionRequest sr : sections) {
                // We don't know the exact count here easily without the List of Lists, but we
                // can pass it or estimate
                // Actually we can just say "Section X carries questions of X marks each"
                list.add(new ListItem(
                        sr.getSectionName() + " carries questions of " + sr.getMarksPerQuestion() + " marks each.",
                        normalFont));
            }
        } else {
            list.add(new ListItem("All questions carry equal marks.", normalFont));
        }

        document.add(list);
        document.add(new Paragraph("______________________________________________________________________________",
                normalFont));
    }
}
