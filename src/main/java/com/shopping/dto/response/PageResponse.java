package com.shopping.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@ApiModel(description = "分页响应")
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前页码")
    private Long current;

    @ApiModelProperty(value = "每页大小")
    private Long size;

    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "总页数")
    private Long pages;

    @ApiModelProperty(value = "数据列表")
    private List<T> records;

    /**
     * 构建分页响应
     */
    public static <T> PageResponse<T> of(Long current, Long size, Long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setCurrent(current);
        response.setSize(size);
        response.setTotal(total);
        response.setPages((total + size - 1) / size); // 计算总页数
        response.setRecords(records);
        return response;
    }
}