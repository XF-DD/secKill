package xfdd.seckill.api.enums;

/**
 * 系统级别的常量
 * @Author: XF-DD
 * @Date: 20/05/19 20:33
 */
public enum SysConstant {
    Invalid(-1,"无效"),
    SuccessButNotPay(0,"成功但未付款"),
    Pay(1,"已付款"),
    Cancel(2,"取消"),

    ;


    private Integer code;
    private String msg;

    SysConstant(int i, String msg) {
        this.code = i;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
