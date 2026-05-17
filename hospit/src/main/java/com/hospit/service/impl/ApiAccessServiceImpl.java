package com.hospit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospit.entity.ApiAccess;
import com.hospit.mapper.ApiAccessMapper;
import com.hospit.service.IApiAccessService;
import org.springframework.stereotype.Service;

// API访问权限服务实现
@Service
public class ApiAccessServiceImpl extends ServiceImpl<ApiAccessMapper, ApiAccess> implements IApiAccessService {
}
