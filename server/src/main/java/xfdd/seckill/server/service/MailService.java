package xfdd.seckill.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import xfdd.seckill.server.dto.MailDto;

import javax.mail.internet.MimeMessage;

/**
 * @Author: XF-DD
 * @Date: 20/05/20 14:48
 */
@Service
@EnableAsync
public class MailService {

    public static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment environment;

    /**
     * 发送简单文本文件
     */
    @Async
    public void sendSimpleEmail(final MailDto dto){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(environment.getProperty("mail.send.from"));
            message.setTo(dto.getTos());
            message.setSubject(dto.getSubject());
            message.setText(dto.getContent());
            mailSender.send(message);
            logger.info("发送简单文本文件-发送成功！");
        }catch(Exception e){
            logger.info("发送简单文本文件-发生异常！",e.fillInStackTrace());
        }
    }

    /**
     * 发送含html邮件
     * @param dto
     */
    public void sendHTMLMail(final MailDto dto){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            mimeMessageHelper.setFrom(environment.getProperty("mail.send.from"));
            mimeMessageHelper.setTo(dto.getTos());
            mimeMessageHelper.setSubject(dto.getSubject());
            mimeMessageHelper.setText(dto.getContent(),true);

            mailSender.send(mimeMessage);
            logger.info("发送花哨邮件-发送成功");
        }catch(Exception e){
            logger.error("发送花哨邮件-发生异常",e.fillInStackTrace());
        }
    }
}
