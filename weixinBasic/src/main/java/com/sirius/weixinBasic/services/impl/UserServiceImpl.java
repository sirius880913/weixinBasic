package com.sirius.weixinBasic.services.impl;

import org.springframework.stereotype.Service;

import com.sirius.weixinBasic.model.User;
import com.sirius.weixinBasic.services.UserService;

@Service("userService")
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService{
}
