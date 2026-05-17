package com.hospit.service.impl;

import com.hospit.entity.User;
import com.hospit.mapper.UserMapper;
import com.hospit.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 科室用户登录信息表 服务实现类
 * </p>
 *
 * @author iamfei2
 * @since 2026-01-17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
