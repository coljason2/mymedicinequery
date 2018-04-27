package com.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.comman.MedEntity;
import com.comman.getQuery;

@Controller
@RequestMapping("/")
public class AppController {
	Logger Log = LoggerFactory.getLogger(AppController.class);
	private String medicine;
	List<MedEntity> meds;

	@RequestMapping(value = { "/login", "/index" }, method = RequestMethod.GET)
	public String login(ModelMap model) {
		return "/login";
	}

	@RequestMapping(value = { "/home" , "/"}, method = RequestMethod.GET)
	public String home(ModelMap model) {
		return "/home";
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	public String POSTQuery(@RequestParam String querystring, Model model) throws UnsupportedEncodingException {
		getMedicine(querystring);
		model.addAttribute("meds", meds);
		model.addAttribute("querystring", medicine);
		return "/resault";
	}

	@RequestMapping(value = "/query/{med}", method = RequestMethod.GET)
	public String GETQuery(@PathVariable("med") String med, Model model) throws UnsupportedEncodingException {
		getMedicine(new String(med.getBytes("ISO-8859-1"), "UTF-8"));
		model.addAttribute("meds", meds);
		model.addAttribute("querystring", medicine);
		return "/resault";
	}

	@RequestMapping(value = "/get/{med}", method = RequestMethod.GET)
	@ResponseBody
	public List<MedEntity> GETQueryapi(@PathVariable("med") String med) throws UnsupportedEncodingException {
		getMedicine(new String(med.getBytes("ISO-8859-1"), "UTF-8"));
		return meds;
	}

	public void getMedicine(String med) throws UnsupportedEncodingException {
		getQuery getmed = new getQuery();
		medicine = med;
		meds = getmed.getMedicine(medicine);
	}
}
