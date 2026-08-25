package com.rafaeldiaz.emisiontvt_ff.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreoReclamo(String destinatario, String nombre, String claimCode, String baseUrl) {
        String enlacePagina = baseUrl + "/claim/" + claimCode;
        String enlaceContrato = baseUrl + "/api/claim/" + claimCode + "/contract";
        String enlaceArteWeb = baseUrl + "/api/claim/" + claimCode + "/fractal-image";

        String asunto = "🌐 Geo Petro Litix AI – Tus tokens y contrato están listos";
        String cuerpo = "<div style='font-family: Arial, sans-serif; max-width:600px; margin:auto;'>"
                + "<h2 style='color:#0d1b2a;'>Hola " + nombre + ",</h2>"
                + "<p>Gracias por invertir en <strong>Geo Petro Litix AI</strong>.</p>"
                + "<p>Hemos confirmado tu pago y generado tus <strong>Tokens de Validación Técnica (TVT)</strong>.</p>"
                + "<p>Puedes ver tu fractal único, descargar el certificado de arte (alta resolución) y tu contrato legal desde los siguientes enlaces:</p>"
                + "<p>🔗 <a href='" + enlacePagina + "'>Ir a mi página de reclamo</a></p>"
                + "<p>🖼️ <a href='" + enlaceArteWeb + "'>Descargar certificado de arte (Web)</a></p>"
                + "<p>📄 <a href='" + enlaceContrato + "'>Descargar contrato legal (PDF)</a></p>"
                + "<p>Si tienes dudas, responde este correo o contáctanos al +57 313 220 3002.</p>"
                + "<p>Atentamente,<br>Equipo Geo Petro Litix AI</p>"
                + "</div>";

        MimeMessage mensaje = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setFrom("alexa@geopetrolitixai.pro");
            helper.setReplyTo("rafael.diaz@geopetrolitixai.pro");
            helper.setText(cuerpo, true); // HTML
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            // Loguear el error, pero no interrumpir el flujo de confirmación
            System.err.println("Error enviando correo: " + e.getMessage());
        }
    }
}