package com.feedss.manage.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.feedss.base.JsonResponse;
import com.feedss.manage.util.Constants;
import com.feedss.portal.entity.Appointment;
import com.feedss.portal.repository.AppointmentRepository;
import com.feedss.user.model.UserVo;

@Controller
@RequestMapping("/manage/appointment")
public class ManageAppointmentController {

	@Autowired
	AppointmentRepository appointmentRepository;

	@RequestMapping(value = "list", method = { RequestMethod.GET, RequestMethod.POST })
	public String list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			HttpSession session, Model model) {
		UserVo user = (UserVo) session.getAttribute(Constants.USER_SESSION);
		Sort sort = new Sort(Direction.DESC, "id");
		Pageable pageable = new PageRequest(page, size, sort);
		Page<Appointment> appointments = appointmentRepository.findAll(pageable);
		
//		model.addAttribute("data", new JsonParser().toJson(appointments));
		model.addAttribute("result", appointments.getContent());
		System.out.println(model);
		return "/manage/appointment/list";
	}
	
	@RequestMapping(value="applyPass",method={RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public ResponseEntity<Object> applyPass(String appointmentId, HttpSession session){
		Appointment appointment = appointmentRepository.findOne(appointmentId);
		appointment.setStatus(1);
		appointmentRepository.save(appointment);
		return JsonResponse.success(appointment);
	}
}
