package com.lzz.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("favorites")
public class Favorite {
    public static final int TYPE_FAVORITE = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long movieId;
    private Integer type;
}
