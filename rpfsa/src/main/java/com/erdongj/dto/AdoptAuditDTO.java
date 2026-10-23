package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 领养申请审核请求。
 * <p>
 * 改造前：前端串行发 3 个请求（改 adopt、改 pet、改 user 领养数），任一步失败都会留下脏数据。
 * 改造后：一个请求进入 Service 的单个事务方法，由服务端完成状态流转与连带更新。
 */
@Data
public class AdoptAuditDTO {

    @NotNull(message = "缺少申请记录 id")
    private Integer id;

    /** 1=审核通过，0=不通过 */
    @NotNull(message = "请选择审核结果")
    private Integer state;

    /** 通过或不通过的原因，会作为通知展示给申请人 */
    private String ponReason;
}
