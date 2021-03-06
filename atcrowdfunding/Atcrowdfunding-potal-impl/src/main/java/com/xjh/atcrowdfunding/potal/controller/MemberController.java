package com.xjh.atcrowdfunding.potal.controller;

import com.xjh.atcrowdfunding.bean.Cert;
import com.xjh.atcrowdfunding.bean.Member;
import com.xjh.atcrowdfunding.bean.MemberCert;
import com.xjh.atcrowdfunding.bean.Ticket;
import com.xjh.atcrowdfunding.manager.service.CertService;
import com.xjh.atcrowdfunding.potal.listener.PassListener;
import com.xjh.atcrowdfunding.potal.listener.RefuseListener;
import com.xjh.atcrowdfunding.potal.service.MemberService;
import com.xjh.atcrowdfunding.potal.service.TicketService;
import com.xjh.atcrowdfunding.util.AjaxResult;
import com.xjh.atcrowdfunding.util.Const;
import com.xjh.atcrowdfunding.vo.Data;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.jfree.chart.axis.Tick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CertService certService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @RequestMapping("/accttype")
    public String accttype(){
        return "member/accttype";
    }

    @RequestMapping("/basicinfo")
    public String basicinfo(){
        return "member/basicinfo";
    }

    @RequestMapping("/uploadCert")
    public String uploadCert(){ return "member/uploadCert"; }

    @RequestMapping("/checkEmail")
    public String checkEmail(){ return "member/checkEmail"; }

    @RequestMapping("/checkAuthCode")
    public String checkAuthCode(){ return "member/checkAuthCode"; }
    @ResponseBody
    @RequestMapping("/updateAcctType")
    public Object updateAcctType(HttpSession session, String accttype){
        AjaxResult ajaxResult=new AjaxResult();
        try{
            Member loginMember=(Member) session.getAttribute(Const.LOGIN_MEMBER);
            loginMember.setAccttype(accttype);
            memberService.updateAccttype(loginMember);

            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());
            ticket.setPstep("accttype");
            ticketService.updatePstep(ticket);

            session.setAttribute(Const.LOGIN_MEMBER,loginMember);
            ajaxResult.setSuccess(true);
        }catch (Exception e){
            ajaxResult.setSuccess(false);
            e.printStackTrace();
        }
        return ajaxResult;
    }

    @ResponseBody
    @RequestMapping("/updateBasicinfo")
    public Object updateBasicinfo(HttpSession session,Member member){
        AjaxResult ajaxResult=new AjaxResult();
        try {
            Member loginMember=(Member) session.getAttribute(Const.LOGIN_MEMBER);
            loginMember.setCardnum(member.getCardnum());
            loginMember.setRealname(member.getRealname());
            loginMember.setTel(member.getTel());
            memberService.updateBasicinfo(loginMember);

            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());
            ticket.setPstep("basicinfo");
            ticketService.updatePstep(ticket);
            ajaxResult.setSuccess(true);
        }catch (Exception e){
            ajaxResult.setSuccess(false);
            e.printStackTrace();
        }
        return ajaxResult;
    }

    @RequestMapping("/apply")
    public String apply(HttpSession session){
            Member loginMember=(Member)session.getAttribute(Const.LOGIN_MEMBER);
            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());
            if (ticket == null){
                ticket = new Ticket();
                ticket.setMemberid(loginMember.getId());
                ticket.setPstep("apply");
                ticket.setStatus("0");
                ticketService.saveTicket(ticket);
            }else {
                String pstep=ticket.getPstep();
                if("accttype".equals(pstep)){
                    return "redirect:basicinfo.htm";
                }else if("basicinfo".equals(pstep)){
                    List<Cert> queryCertByAccttype=certService.queryCertByAccttype(loginMember.getAccttype());
                    session.setAttribute("queryCertByAccttype",queryCertByAccttype);
                    return "redirect:uploadCert.htm";
                }else if("uploadCert".equals(pstep)){
                    return "redirect:checkEmail.htm";
                }else if ("checkEmail".equals(pstep)){
                    return "redirect:checkAuthCode.htm";
                }
            }
        return "redirect:accttype.htm";
    }

    @ResponseBody
    @RequestMapping("/doUploadCert")
    public Object doUploadCert(HttpSession session, Data ds){
        AjaxResult ajaxResult=new AjaxResult();
        try {
            //????????????????????????
            Member loginMember=(Member)session.getAttribute(Const.LOGIN_MEMBER);

            //?????????????????????????????????
            String realPath=session.getServletContext().getRealPath("/pics");
            List<MemberCert> certimgs=ds.getCertimgs();
            for (MemberCert memberCert:certimgs){
                MultipartFile fileImg=memberCert.getFileImg();
                String extName=fileImg.getOriginalFilename().substring(fileImg.getOriginalFilename().lastIndexOf("."));
                String tmpName=UUID.randomUUID().toString()+extName;
                String filename=realPath+"/cert"+"/"+ tmpName;

                fileImg.transferTo(new File(filename));//??????????????????

                //????????????
                memberCert.setIconpath(tmpName);//??????????????????????????????
                memberCert.setMemberid(loginMember.getId());
            }

            //??????????????????????????????
            certService.saveMemberCert(certimgs);

            //??????????????????
            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());
            ticket.setPstep("uploadCert");
            ticketService.updatePstep(ticket);
            ajaxResult.setSuccess(true);
        }catch (Exception e){
            ajaxResult.setSuccess(false);
            e.printStackTrace();
        }
        return ajaxResult;
    }


    @ResponseBody
    @RequestMapping("/startProcess")
    public Object startProcess(HttpSession session, String email){

        AjaxResult ajaxResult=new AjaxResult();
        try {
            //????????????????????????
            Member loginMember=(Member)session.getAttribute(Const.LOGIN_MEMBER);

            //????????????????????????Email??????????????????????????????
            if (!loginMember.getEmail().equals(email)){
                loginMember.setEmail(email);
                memberService.updateEmail(loginMember);
            }

            //???????????????????????? - ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().processDefinitionKey("auth").singleResult();

            StringBuilder authcode=new StringBuilder();
            for (int i=0;i<4;i++){
                authcode.append(new Random().nextInt(10));
            }

            Map<String,Object> variables=new HashMap<>();
            variables.put("toEmail",email);
            variables.put("authcode",authcode);
            variables.put("loginacct",loginMember.getLoginacct());
            variables.put("passListener",new PassListener());
            variables.put("refuseListener",new RefuseListener());

            //ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("auth");
            ProcessInstance processInstance=runtimeService.startProcessInstanceById(processDefinition.getId(),variables);

            //??????????????????
            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());
            ticket.setPstep("checkEmail");
            ticket.setPiid(processInstance.getId());
            ticket.setAuthcode(authcode.toString());
            ticketService.updatePiidAndPstep(ticket);
            ajaxResult.setSuccess(true);
        }catch (Exception e){
            ajaxResult.setSuccess(false);
            e.printStackTrace();
        }
        return ajaxResult;
    }

    @ResponseBody
    @RequestMapping("/finishApply")
    public Object finishApply(HttpSession session, String authcode){

        AjaxResult ajaxResult=new AjaxResult();
        try {
            //????????????????????????
            Member loginMember=(Member)session.getAttribute(Const.LOGIN_MEMBER);

            //????????????????????????????????????????????????
            Ticket ticket=ticketService.getTicketByMemberId(loginMember.getId());

            if (ticket.getAuthcode().equals(authcode)){
                //???????????????????????????
                Task task=taskService.createTaskQuery().processInstanceId(ticket.getPiid()).taskAssignee(loginMember.getLoginacct()).singleResult();
                taskService.complete(task.getId());

                //????????????????????????
                loginMember.setAuthstatus("1");
                memberService.updateAuthstatus(loginMember);
                //??????????????????
                ticket.setPstep("finishApply");
                ticketService.updatePstep(ticket);
                ajaxResult.setSuccess(true);
            }else{
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("???????????????????????????????????????");
            }
        }catch (Exception e){
            ajaxResult.setSuccess(false);
            e.printStackTrace();
        }
        return ajaxResult;
    }
}
