package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.PeixunEntity;

import com.service.PeixunService;
import com.entity.view.PeixunView;
import com.service.YonghuService;
import com.entity.YonghuEntity;

import com.utils.PageUtils;
import com.utils.R;

/**
 * 培训
 * 后端接口
 * @author
 * @email
 * @date 2021-04-25
*/
@RestController
@Controller
@RequestMapping("/peixun")
public class PeixunController {
    private static final Logger logger = LoggerFactory.getLogger(PeixunController.class);

    @Autowired
    private PeixunService peixunService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
     
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = peixunService.queryPage(params);

        //字典表数据转换
        List<PeixunView> list =(List<PeixunView>)page.getList();
        for(PeixunView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        PeixunEntity peixun = peixunService.selectById(id);
        if(peixun !=null){
            //entity转view
            PeixunView view = new PeixunView();
            BeanUtils.copyProperties( peixun , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(peixun.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody PeixunEntity peixun, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,peixun:{}",this.getClass().getName(),peixun.toString());
        Wrapper<PeixunEntity> queryWrapper = new EntityWrapper<PeixunEntity>()
            .eq("yonghu_id", peixun.getYonghuId())
            .eq("peixun_name", peixun.getPeixunName())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PeixunEntity peixunEntity = peixunService.selectOne(queryWrapper);
        if(peixunEntity==null){
            peixun.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      peixun.set
        //  }
            peixunService.insert(peixun);
            return R.ok();
        }else {
            return R.error(511,"该用户已经在本次培训中存在");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody PeixunEntity peixun, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,peixun:{}",this.getClass().getName(),peixun.toString());
        //根据字段查询是否有相同数据
        Wrapper<PeixunEntity> queryWrapper = new EntityWrapper<PeixunEntity>()
            .notIn("id",peixun.getId())
            .andNew()
            .eq("yonghu_id", peixun.getYonghuId())
            .eq("peixun_name", peixun.getPeixunName())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PeixunEntity peixunEntity = peixunService.selectOne(queryWrapper);
        if(peixunEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      peixun.set
            //  }
            peixunService.updateById(peixun);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"该用户已经在本次培训中存在");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        peixunService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }






}

