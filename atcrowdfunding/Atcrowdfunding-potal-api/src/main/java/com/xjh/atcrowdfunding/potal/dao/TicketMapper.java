package com.xjh.atcrowdfunding.potal.dao;

import com.xjh.atcrowdfunding.bean.Member;
import com.xjh.atcrowdfunding.bean.Ticket;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMapper {
    int deleteByPrimaryKey(Integer id);

    Ticket selectByPrimaryKey(Integer id);

    List<Ticket> selectAll();

	Ticket getTicketByMemberId(Integer memberid);

	void saveTicket(Ticket ticket);

	void updatePstep(Ticket ticket);

	void updatePiidAndPstep(Ticket ticket);

    Member getMemberByPiid(String processInstanceId);

    void updateStatus(Member member);
}