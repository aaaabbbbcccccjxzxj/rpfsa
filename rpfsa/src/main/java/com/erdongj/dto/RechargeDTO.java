package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 充值请求。金额必须在服务端白名单档位内，杜绝任意金额伪造。
 */
@Data
public class RechargeDTO {

    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于 0")
    private BigDecimal amount;
}
