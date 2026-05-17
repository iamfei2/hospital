package com.hospit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospit.entity.SearchTemplate;
import com.hospit.mapper.SearchTemplateMapper;
import com.hospit.service.ISearchTemplateService;
import org.springframework.stereotype.Service;

// 查询模板服务实现
@Service
public class SearchTemplateServiceImpl extends ServiceImpl<SearchTemplateMapper, SearchTemplate> implements ISearchTemplateService {
}
