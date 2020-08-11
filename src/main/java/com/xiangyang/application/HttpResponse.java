package com.xiangyang.application;

import lombok.Data;

import java.io.Serializable;
@Data
public class HttpResponse implements Serializable {
    private static final long serialVersionUID = 6767041216879833087L;
    private boolean success;//成功标志

    private String code;//信息码

    private String description;//描述

}
