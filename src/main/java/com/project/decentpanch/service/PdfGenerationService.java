package com.project.decentpanch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.project.decentpanch.DTOs.BirthCertificateDTO;
import com.project.decentpanch.DTOs.CasteCertificateDTO;
import com.project.decentpanch.DTOs.DeathCertificateDTO;
import com.project.decentpanch.entity.Certificate;
import com.project.decentpanch.entity.Panchayat;
import com.project.decentpanch.exceptions.ResourceNotFoundException;
import com.project.decentpanch.repo.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class PdfGenerationService {

    @Autowired
    PanchayatService panchayatService;

    @Autowired
    CertificateRepository certificateRepository;

    private void generateBirthPDF(Document document, PdfDocument pdf, BirthCertificateDTO dto, Panchayat panchayat, Certificate cert) throws IOException {
        addHeader(document, "BIRTH CERTIFICATE", panchayat);

        addCertificateDetailsTable(document,
                new String[]{"Name", dto.getChildName()},
                new String[]{"Date of Birth", dto.getDob()},
                new String[]{"Place of Birth", dto.getPlaceOfBirth()},
                new String[]{"Gender", dto.getGender()},
                new String[]{"Father's Name", dto.getFatherName()},
                new String[]{"Mother's Name", dto.getMotherName()},
                new String[]{"Registration No", cert.getRegistrationNo()},
                new String[]{"Date of Registration", cert.getIssuedDate().toString()}
        );

        addQRCodeAndFooter(document, pdf, cert.getRegistrationNo(), panchayat);
    }


    private void generateDeathPDF(Document document, PdfDocument pdf, DeathCertificateDTO dto, Panchayat panchayat, Certificate cert) throws IOException {
        // Add Header with Panchayat details
        addHeader(document, "DEATH CERTIFICATE", panchayat);

        // Add Certificate Details Table with Certificate-specific details from the DTO
        addCertificateDetailsTable(document,
                new String[]{"Deceased Name", dto.getDeceasedName()},
                new String[]{"Date of Birth", dto.getDob()},
                new String[]{"Date of Death", dto.getDod()},
                new String[]{"Place of Death", dto.getPlaceOfDeath()},
                new String[]{"Cause of Death", dto.getCauseOfDeath()},
                new String[]{"Gender", dto.getGender()},
                new String[]{"Registration No", cert.getRegistrationNo()},
                new String[]{"Date of Registration", cert.getIssuedDate().toString()}
        );

        // Add QR Code and Footer with the Panchayat details
        addQRCodeAndFooter(document, pdf, cert.getRegistrationNo(), panchayat);
    }

    private void generateCastePDF(Document document, PdfDocument pdf, CasteCertificateDTO dto, Panchayat panchayat, Certificate cert) throws IOException {
        // Add Header with Panchayat details
        addHeader(document, "CASTE CERTIFICATE", panchayat);

        // Add Certificate Details Table with Certificate-specific details from the DTO
        addCertificateDetailsTable(document,
                new String[]{"Person Name", dto.getPersonName()},
                new String[]{"Caste", dto.getCaste()},
                new String[]{"Sub-Caste", dto.getSubCaste()},
                new String[]{"Address", dto.getAddress()},
                new String[]{"Registration No", cert.getRegistrationNo()},
                new String[]{"Date of Issue", cert.getIssuedDate().toString()}
        );

        // Add QR Code and Footer with the Panchayat details
        addQRCodeAndFooter(document, pdf, cert.getRegistrationNo(), panchayat);
    }



    private void addHeader(Document document, String certificateTitle, Panchayat panchayat) throws IOException {
        // Create table with 2 columns
        float[] columnWidths = {100, 400}; // Adjust as needed
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // --- Left Column: Logo ---
        byte[] logoBytes = panchayat.getLogoImage();
        if (logoBytes != null) {
            ImageData imageData = ImageDataFactory.create(logoBytes);
            Image logo = new Image(imageData)
                    .setWidth(80)
                    .setHeight(80)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            Cell logoCell = new Cell()
                    .add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addCell(logoCell);
        } else {
            table.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        // --- Right Column: Panchayat Name, Title, Issuance (Stacked & Centered) ---
        Paragraph rightContent = new Paragraph()
                .add(new Text(panchayat.getPanchayatName() + "\n").setBold().setFontSize(18).setFontColor(ColorConstants.RED))
                .add(new Text(certificateTitle + "\n").setBold().setFontSize(22).setFontColor(ColorConstants.GREEN))
                .add(new Text("(Issued Under Section 12/17)").setFontSize(12));

        Cell textCell = new Cell()
                .add(rightContent)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);  // **Important: Center align horizontally**

        table.addCell(textCell);

        // Add table to document
        document.add(table);
    }
    private void addCertificateDetailsTable(Document document, String[]... details) {
        Table table = new Table(2).setWidth(UnitValue.createPercentValue(100));
        for (String[] detail : details) {
            table.addCell(new Cell().add(new Paragraph(detail[0] + ":").setBold()));
            table.addCell(new Cell().add(new Paragraph(detail[1])));
        }
        document.add(table);
    }


    private void addQRCodeAndFooter(Document document, PdfDocument pdf, String registrationNo, Panchayat panchayat) {
        // Generate QR code with the registration number
        BarcodeQRCode qrCode = new BarcodeQRCode("https://verify.certificate.com?id=" + registrationNo);
        Image qrImage = new Image(qrCode.createFormXObject(pdf)).setWidth(100).setTextAlignment(TextAlignment.LEFT);
        document.add(qrImage);

        // Add certification authority details
        document.add(new Paragraph("\nCertified by").setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(panchayat.getPanchayatName())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());
        document.add(new Paragraph("\n\n__________________________").setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Authorized Signatory").setTextAlignment(TextAlignment.RIGHT).setBold());
    }

    public File generateCertificatePDF(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + certificateId + " not found"));

        Panchayat panchayat = panchayatService.getPanchayatById(certificate.getPanchayat().getPanchayatId());
        String dest = System.getProperty("java.io.tmpdir") + "/" + certificate.getType() + "_Certificate.pdf";

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(dest));
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            ObjectMapper mapper = new ObjectMapper();
            String type = certificate.getType().toUpperCase();

            switch (type) {
                case "BIRTH":
                    BirthCertificateDTO birthData = mapper.readValue(certificate.getDetails().toString(), BirthCertificateDTO.class);
                    generateBirthPDF(document, pdf, birthData, panchayat, certificate);
                    break;

                case "DEATH":
                    DeathCertificateDTO deathData = mapper.readValue(certificate.getDetails().toString(), DeathCertificateDTO.class);
                    generateDeathPDF(document, pdf, deathData, panchayat, certificate);
                    break;

                case "CASTE":
                    CasteCertificateDTO casteData = mapper.readValue(certificate.getDetails().toString(), CasteCertificateDTO.class);
                    generateCastePDF(document, pdf, casteData, panchayat, certificate);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown certificate type: " + type);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error generating certificate PDF", e);
        }

        return new File(dest);
    }

}
