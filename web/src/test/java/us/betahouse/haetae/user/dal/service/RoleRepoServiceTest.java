package us.betahouse.haetae.user.dal.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.betahouse.haetae.serviceimpl.user.enums.UserRoleCode;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.model.basic.perm.RoleBO;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRepoServiceTest {

    @Autowired
    private RoleRepoService roleRepoService;
    @Autowired
    private UserInfoRepoService userInfoRepoService;

    @Test
    public void createRole() {
        RoleBO roleBO = new RoleBO();
        roleBO.setRoleCode("LEAGUE_MANAGER");
        roleBO.setRoleName("团委管理员");
//        roleBO.setRoleDesc("测试权限");
        System.out.println(roleRepoService.createRole(roleBO));
    }

    @Test
    public void addCertificateManager(){
        UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId("17905711");
        if(null==userInfoBO){
            System.out.println("账号不存在");
            return ;
        }
        else{
            roleRepoService.userBindRolesByCode(userInfoBO.getUserId(), UserRoleCode.CERTIFICATE_MANAGER);
            System.out.println("绑定成功");
        }
    }


   @Test
    public void queryRolesByRoleIds() {
    }

    @Test
    public void queryRolesByUserId() {
    }

    @Test
    public void userBindRoles() {
        roleRepoService.userBindRoles("123456789", Collections.singletonList("201811171435334621498500021117"));

    }

    @Test
    public void userBindRolesByCode() {
    }

    @Test
    public void userUnbindRoles() {
    }

    @Test
    public void usersBindRole() {
    }

    @Test
    public void usersUnbindRole() {
    }

    @Test
    public void detachAllUser() {
    }

    @Test
    public void initRole() {
    }
}