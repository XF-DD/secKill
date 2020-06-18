package xfdd.seckill.server.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

/**
 * @Author: XF-DD
 * @Date: 20/05/19 18:00
 */
@Data
@ToString
public class KillDto {

    @NonNull
    private Integer killId;

    private Integer userId;
}
