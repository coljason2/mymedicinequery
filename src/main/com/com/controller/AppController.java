package com.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.comman.MedEntity;
import com.comman.getQuery;

@Controller
public class AppController {
	Logger Log = LoggerFactory.getLogger(AppController.class);

	@RequestMapping(value = { "/", "/home", "/index" }, method = RequestMethod.GET)
	public String home(ModelMap model) {
		return "/index";
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	public String Query(@RequestParam String querystring, Model model) throws UnsupportedEncodingException {
		List<MedEntity> meds = new ArrayList<MedEntity>();
		Log.info("med = {} ", new String(querystring.getBytes("ISO-8859-1"), "UTF-8"));
		getQuery getmed = new getQuery();
		String medicine = new String(querystring.getBytes("ISO-8859-1"), "UTF-8");
		// Log.info(medicine);
		meds = getmed.getMedicine(medicine);
		model.addAttribute("meds", meds);
		model.addAttribute("querystring", medicine);
		return "/resault";
	}

}
