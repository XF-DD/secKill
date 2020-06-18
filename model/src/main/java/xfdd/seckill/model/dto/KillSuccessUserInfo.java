package xfdd.seckill.model.dto;

import lombok.Data;
import xfdd.seckill.model.entity.ItemKillSuccess;

import java.io.Serializable;

/**
 * @Author: XF-DD
 * @Date: 20/05/20 11:59
 */
@Data
public class KillSuccessUserInfo extends ItemKillSuccess implements Serializable {

    private String userName;

    private String phone;

    private String email;

    private String itemName;

    @Override
    public String toString() {
        return super.toString()+"\nKillSuccessUserInfo{" +
                "userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
