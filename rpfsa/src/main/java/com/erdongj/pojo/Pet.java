package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/9 17:03
 * @Description: 宠物实体表
 */
@Data
@NoArgsConstructor
public class Pet {

    /** 来源类型：平台流浪 */
    public static final int TYPE_STRAY = 0;
    /** 来源类型：私人上传 */
    public static final int TYPE_PRIVATE = 1;

    /** 领养状态：流浪中 */
    public static final int ADOPT_STRAY = 0;
    /** 领养状态：已被领养 */
    public static final int ADOPT_DONE = 1;
    /** 领养状态：审核中 */
    public static final int ADOPT_AUDITING = 2;

    /** 品种分类：狗 */
    public static final int CLASSIC_DOG = 0;
    /** 品种分类：猫 */
    public static final int CLASSIC_CAT = 1;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String petname;
    private String photo;
    private Integer sex;
    private Integer age;
    private Integer type;
    private Integer classic;
    private String variety;
    private Integer isVaccine;
    private Integer isAdopt;
    private Integer isDel;
    private Integer petCoin;
    private String describtion;
    private Integer commentCount;
    @TableField(exist = false)
    private List<Comment> comments; //用来存放该条帖子的评论
    @TableField(exist = false)
    private List<PetPhoto> photos; //相册（多图），详情接口装配
    private Date createTime;
    @TableField(exist = false)
    private User user;
    private Integer uid; //收养人id

}
