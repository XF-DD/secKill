package xfdd.seckill.server.service;

/**
 * @Author: XF-DD
 * @Date: 20/05/19 17:54
 */
public interface IKillService {
    boolean killItem(int killId,int userId) throws Exception;

    boolean killItemV2(int killId,int userId) throws Exception;

    boolean killItemV3(int killId,int userId) throws Exception;

    boolean killItemV4(int killId,int userId) throws Exception;

    boolean killItemV5(int killId,int userId) throws Exception;
}
