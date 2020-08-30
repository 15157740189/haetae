/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.activity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.model.activity.PastActivityVO;
import us.betahouse.haetae.model.activity.request.ActivityRestRequest;
import us.betahouse.haetae.model.activity.request.AuditRestRequest;
import us.betahouse.haetae.serviceimpl.activity.enums.ActivityOperationEnum;
import us.betahouse.haetae.serviceimpl.activity.model.AuditMessage;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityManagerRequest;
import us.betahouse.haetae.serviceimpl.activity.request.builder.ActivityManagerRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.utils.AuditUtil;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.haetae.serviceimpl.schedule.manager.AccessTokenManage;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.log.Log;
import us.betahouse.util.utils.AssertUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * 活动接口
 *
 * @author MessiahJK
 * @version : ActivityController.java 2018/11/25 13:16 MessiahJK
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/activity")
public class ActivityController {
    /**
     * 日志实体
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    /**
     * 添加活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PostMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> add(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "新增活动", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动名不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityType(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动类型不能为空");
                AssertUtil.assertNotNull(request.getActivityStartTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动开始时间不能为空");
                AssertUtil.assertNotNull(request.getActivityEndTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动结束时间不能为空");
                boolean validateTime = new Date(request.getActivityStartTime()).before(new Date(request.getActivityEndTime()));
                AssertUtil.assertTrue(validateTime, "活动开始时间必须早于结束时间");
                AssertUtil.assertNotNull(request.getOrganizationMessage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "举办单位不能为空");
            }

            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withUserId(request.getUserId())
                        .withActivityName(request.getActivityName())
                        .withType(request.getActivityType())
                        .withOrganizationMessage(request.getOrganizationMessage())
                        .withStart(request.getActivityStartTime())
                        .withEnd(request.getActivityEndTime())
                        .withTerm(request.getTerm() == null ? TermUtil.getNowTerm() : request.getTerm())
                        // 以下是可选参数
                        // 描述
                        .withDescription(request.getDescription())
                        // 地点
                        .withLocation(request.getLocation())
                        // 分数
                        .withScore(request.getScore())
                        .build();
                ActivityBO activityBO = activityService.create(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "创建活动成功");
            }
        });
    }

    /**
     * 获取所有活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getActivityList(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();

                // 填充状态选择条件
                if (StringUtils.isNotBlank(request.getState())) {
                    builder.withState(request.getState());
                }

                // 添加学期选择条件
                if (StringUtils.isNotBlank(request.getTerm())) {
                    builder.withTerm(request.getTerm());
                }

                // 添加类型选择条件
                if(StringUtils.isNotBlank(request.getActivityType())){
                    builder.withType(request.getActivityType());
                }

                //添加页码
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }

                //添加每页条数
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }

                //添加排序規則
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                return RestResultUtil.buildSuccessResult(activityService.findAll(builder.build(), context), "获取活动列表成功");
            }
        });
    }

    /**
     * 校验活动是否存在
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/check")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> checkActivity(ActivityRestRequest request,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "校验活动是否存在且处于发布状态", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
            }
            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId()).build();
                Boolean ifActivityExist=activityService.checkActivity(activityManagerRequest,context);
                return ifActivityExist ? RestResultUtil.buildSuccessResult("活动存在并处于发布状态"):RestResultUtil.buildFailResult("活动不处于发布状态");
            }
        });
    }


    /**
     * 操作活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> operate(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getOperation(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "操作不能为空");
            }

            @Override
            public Result<ActivityBO> execute() {
                // 强校验操作类型
                ActivityOperationEnum operation = ActivityOperationEnum.getByCode(request.getOperation());
                AssertUtil.assertNotNull(operation, "操作类型不存在");
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .withUserId(request.getUserId())
                        .withOperation(request.getOperation())
                        .build();
                ActivityBO activityBO = activityService.operate(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, MessageFormat.format("活动{0}成功", operation.getDesc()));
            }
        });
    }
    /**
     * 获取以往活动记录
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> getPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<PastActivityVO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(PastActivityVO.valueOf(activityService.getPastActivity(activityManagerRequest, context)));
            }
        });
    }
    /**
     * 分配未分配活动章
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PostMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> assignPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertNotNull(request.getUndistributedStamp(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "未分配活动章数不能为空");
                AssertUtil.assertNotNull(request.getPastSchoolActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "校园活动章数不能为空");
                AssertUtil.assertNotNull(request.getPastLectureActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "讲座活动章数不能为空");
            }

            @Override
            public Result<PastActivityVO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                activityManagerRequest.setUndistributedStamp(request.getUndistributedStamp());
                activityManagerRequest.setPastSchoolActivity(request.getPastSchoolActivity());
                activityManagerRequest.setPastLectureActivity(request.getPastLectureActivity());
                activityService.assignPastRecord(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(PastActivityVO.valueOf(activityService.getPastActivity(activityManagerRequest, context)));
            }
        });
    }

    @CheckLogin
    @PostMapping("/audit")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<AuditRestRequest> auditActivity(AuditRestRequest request ,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "审核结果发布", request, new RestOperateCallBack<AuditRestRequest>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getResult(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核结果不能为空");
                AssertUtil.assertStringNotBlank(request.getDetail(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核内容不能为空");
                AssertUtil.assertStringNotBlank(request.getAuditTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核时间不能为空");
                AssertUtil.assertStringNotBlank(request.getApplicant(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "申请人不能为空");
            }

            @Override
            public Result<AuditRestRequest> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                AuditMessage message = new AuditMessage();
                BeanUtils.copyProperties(request,message);
                String openid =  userService.queryByUserId(request.getUserId(),context).getOpenId();
                String token = AccessTokenManage.GetToken();;
               String result = AuditUtil.publishAuditByOpenId(openid,token,message);
                if (StringUtils.equals(CommonResultCode.FORBIDDEN.getCode(),result)){
                    return  RestResultUtil.buildSuccessResult( request , "用户未允许订阅该消息");
                }
                return RestResultUtil.buildSuccessResult(request , "订阅信息已发布");;
            }

            });
        }
}
