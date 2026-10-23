package com.erdongj.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 捐助提交请求。
 * <p>
 * 改造前：前端把算好的总价、扣减后的余额、序列化好的清单一起提交，服务端照单全收。
 * 改造后：前端只提交"救助哪只宠物、选了哪些救助品各几件"，
 * 总价、余额校验、库存校验全部由服务端重算，前端无法伪造金额。
 */
@Data
public class DonateCommitDTO {

    @NotNull(message = "请选择要救助的宠物")
    private Integer petId;

    @NotEmpty(message = "请至少选择一件救助品")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "救助品不能为空")
        private Integer foodId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "单件救助品数量至少为 1")
        private Integer count;
    }
}
