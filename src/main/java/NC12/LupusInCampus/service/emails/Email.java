package NC12.LupusInCampus.service.emails;

import NC12.LupusInCampus.utils.LoggerUtil;

import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {

    MailProperties mailProperties = MailProperties.getInstance();

    private static Properties props;
    private static Authenticator auth;
    private static Session session;


    private static Email instance;
    public static Email getInstance() {
        if (instance == null) {
            instance = new Email();
        }
        return instance;
    }

    private Email() {
        LoggerUtil.logInfo("SSLEmail Started");
        props = getProps();
        auth = getAuthenticator();
        session = getSession();
        LoggerUtil.logInfo("Session created");
    }
    private Properties getProps() {
        props = new Properties();
        props.put("mail.smtp.host", this.mailProperties.getHost()); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", this.mailProperties.isAuth()); //Enabling SMTP Authentication
        props.put("mail.smtp.port", this.mailProperties.getPort()); //SMTP Port
        return props;
    }

    private Authenticator getAuthenticator() {

        String username = this.mailProperties.getUsername();
        String password = this.mailProperties.getPassword();
        return auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    private Session getSession() {
        return Session.getDefaultInstance(props, auth);
    }

    public void sendEmail(String toEmail, String subject, String body){
        try
        {
            if (session == null){
                props = getProps();
                auth = getAuthenticator();
                session = getSession();
            }

            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(this.mailProperties.getHost(), "Lupus In Campus"));

            msg.setReplyTo(InternetAddress.parse(this.mailProperties.getHost(), false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            LoggerUtil.logInfo("Message is ready");
            Transport.send(msg);

            LoggerUtil.logInfo("Email Sent Successfully!!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}