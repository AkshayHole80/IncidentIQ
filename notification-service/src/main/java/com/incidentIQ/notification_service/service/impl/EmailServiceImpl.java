package com.incidentIQ.notification_service.service.impl;

import com.incidentIQ.notification_service.event.IncidentNotificationEvent;
import com.incidentIQ.notification_service.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public ResponseEntity<String> sendTestEmail(String to) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("incidentiq.system@gmail.com");
            message.setTo(to);
            message.setSubject("IncidentIQ Email Test");

            message.setText("""
                    Hello,

                    Congratulations!

                    Your Notification Service is successfully sending emails.

                    This email was sent from IncidentIQ.

                    Regards,
                    IncidentIQ Team
                    """);

            mailSender.send(message);

            return ResponseEntity.ok("Email sent successfully.");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email : " + e.getMessage());
        }
    }

    @Override
    public void sendAssignmentEmail(IncidentNotificationEvent event) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("incidentiq.system@gmail.com");
            helper.setTo(event.getRecipientEmail());

            helper.setSubject("Incident Assigned | IncidentIQ");

            String html = """
                <html>
                <body style="font-family:Arial">

                    <h2 style="color:#2563eb">
                        Incident Assigned
                    </h2>

                    <p>Hello <b>%s</b>,</p>

                    <p>A new incident has been assigned to you.</p>

                    <table border="1"
                           cellpadding="8"
                           cellspacing="0">

                        <tr>
                            <td><b>Incident ID</b></td>
                            <td>%d</td>
                        </tr>

                        <tr>
                            <td><b>Priority</b></td>
                            <td>%s</td>
                        </tr>

                        <tr>
                            <td><b>Assigned By</b></td>
                            <td>%s</td>
                        </tr>

                    </table>

                    <br>

                    <p>Please login to IncidentIQ and start working on this incident.</p>

                    <br>

                    <p>
                        Regards,<br>
                        IncidentIQ Team
                    </p>

                </body>
                </html>
                """.formatted(
                    event.getRecipientName(),
                    event.getIncidentId(),
                    event.getPriority(),
                    event.getAssignedBy()
            );

            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {

            log.error("Failed to send assignment email for incident {} to recipient {}: {}", 
                    event.getIncidentId(), event.getRecipientEmail(), e.getMessage(), e);

        }

    }

    @Override
    public void sendCriticalIncidentEmail(
            IncidentNotificationEvent event) {

        try {

            MimeMessage mimeMessage =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("incidentiq.system@gmail.com");

            helper.setTo(event.getRecipientEmail());

            helper.setSubject("🚨 Critical Incident Alert");

            String html = """
                <html>
                <body style="font-family:Arial">

                <h2 style="color:red">
                    Critical Incident Raised
                </h2>

                <p>Hello <b>%s</b>,</p>

                <p>
                    A <b>CRITICAL</b> incident has been created.
                </p>

                <table border="1"
                       cellpadding="8"
                       cellspacing="0">

                    <tr>
                        <td><b>Incident ID</b></td>
                        <td>%d</td>
                    </tr>

                    <tr>
                        <td><b>Priority</b></td>
                        <td>%s</td>
                    </tr>

                    <tr>
                        <td><b>Raised By</b></td>
                        <td>%s</td>
                    </tr>

                </table>

                <br>

                <p>
                    Immediate attention is required.
                </p>

                <br>

                Regards,<br>
                IncidentIQ Team

                </body>
                </html>
                """.formatted(
                    event.getRecipientName(),
                    event.getIncidentId(),
                    event.getPriority(),
                    event.getAssignedBy()
            );

            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {

            log.error("Failed to send critical incident email for incident {} to recipient {}: {}", 
                    event.getIncidentId(), event.getRecipientEmail(), e.getMessage(), e);

        }

    }

    @Override
    public void sendResolutionEmail(IncidentNotificationEvent event) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("incidentiq.system@gmail.com");
            helper.setTo(event.getRecipientEmail());
            helper.setSubject("Incident Resolved | IncidentIQ");

            String html = """
                <html>
                <body style="font-family:Arial">
                    <h2 style="color:#10b981">Incident Resolved</h2>
                    <p>Hello <b>%s</b>,</p>
                    <p>Your incident has been resolved.</p>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr><td><b>Incident ID</b></td><td>%d</td></tr>
                        <tr><td><b>Priority</b></td><td>%s</td></tr>
                        <tr><td><b>Resolved By</b></td><td>%s</td></tr>
                    </table>
                    <br>
                    <p>If you are satisfied with the resolution, no further action is needed. Otherwise, please reply or contact support.</p>
                    <br>
                    <p>Regards,<br>IncidentIQ Team</p>
                </body>
                </html>
                """.formatted(
                    event.getRecipientName(),
                    event.getIncidentId(),
                    event.getPriority(),
                    event.getAssignedBy()
            );

            helper.setText(html, true);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            log.error("Failed to send resolution email for incident {} to recipient {}: {}", 
                    event.getIncidentId(), event.getRecipientEmail(), e.getMessage(), e);
        }
    }

    @Override
    public void sendClosureEmail(IncidentNotificationEvent event) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("incidentiq.system@gmail.com");
            helper.setTo(event.getRecipientEmail());
            helper.setSubject("Incident Closed | IncidentIQ");

            String html = """
                <html>
                <body style="font-family:Arial">
                    <h2 style="color:#6b7280">Incident Closed</h2>
                    <p>Hello <b>%s</b>,</p>
                    <p>Your incident has been closed.</p>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr><td><b>Incident ID</b></td><td>%d</td></tr>
                        <tr><td><b>Priority</b></td><td>%s</td></tr>
                        <tr><td><b>Closed By</b></td><td>%s</td></tr>
                    </table>
                    <br>
                    <p>This is a final confirmation that the incident has been archived.</p>
                    <br>
                    <p>Regards,<br>IncidentIQ Team</p>
                </body>
                </html>
                """.formatted(
                    event.getRecipientName(),
                    event.getIncidentId(),
                    event.getPriority(),
                    event.getAssignedBy()
            );

            helper.setText(html, true);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            log.error("Failed to send closure email for incident {} to recipient {}: {}", 
                    event.getIncidentId(), event.getRecipientEmail(), e.getMessage(), e);
        }
    }
}