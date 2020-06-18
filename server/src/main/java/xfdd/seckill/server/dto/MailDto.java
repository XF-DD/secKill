package xfdd.seckill.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: XF-DD
 * @Date: 20/05/20 15:37
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MailDto {
    //邮件主题
    private String subject;
    //邮件内容
    private String content;
    //接收人
    private String[] tos;


}
