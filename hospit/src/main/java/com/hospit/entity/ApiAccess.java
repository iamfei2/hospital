package com.hospit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("api_access")
@Schema(description = "对外API访问凭证表")
public class ApiAccess implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "access_id", type = IdType.AUTO)
    @Schema(description = "访问ID")
    private Long accessId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "API密文")
    private String apiSecret;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "速率限制")
    private Integer rateLimit;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public Long getAccessId() { return accessId; }
    public void setAccessId(Long accessId) { this.accessId = accessId; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getRateLimit() { return rateLimit; }
    public void setRateLimit(Integer rateLimit) { this.rateLimit = rateLimit; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
