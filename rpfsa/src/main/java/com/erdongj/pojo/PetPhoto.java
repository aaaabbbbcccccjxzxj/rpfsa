package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 宠物相册：一只宠物多张照片，按 sort 升序展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetPhoto {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer pid;
    private String url;
    private Integer sort;
    private Date createTime;
}
