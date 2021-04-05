package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName=principal.getName();
		System.out.println("Username: "+userName);
		User user=userRepository.getUserByUserName(userName);
		System.out.println("User: "+user);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "Home");
		
		return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
		
	}
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage")MultipartFile file, Principal principal, HttpSession sessio) {
		try{String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		if(file.isEmpty()) {
			System.out.println("File is empty");
			contact.setImage("default.jpg");

			
		}else {
			contact.setImage(file.getOriginalFilename());
			File saveFile=new ClassPathResource("static/img").getFile();
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
		user.getContacts().add(contact);
		contact.setUser(user);
	
		this.userRepository.save(user);
	System.out.println(contact);
	sessio.setAttribute("message", new Message("Your contact is added successfully, Add more", "success"));
		
		}
		catch(Exception e) {
			System.out.println("Error"+e.getMessage());
			sessio.setAttribute("message", new Message("Somethong went wrong, Try again..., Add more", "danger"));



		}
		return "normal/add_contact_form";
	}
	@RequestMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model, Principal p) {
		
		model.addAttribute("title", "Show Contacts");
		String userName=p.getName();
		User user=this.userRepository.getUserByUserName(userName);
		//currentpage-page
		//contact per page-5
		Pageable pageable=PageRequest.of(page, 5);
		Page<Contact>contacts=	this.contactRepository.findContactByUserId(user.getId(), pageable);
		model.addAttribute("contact", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	@RequestMapping("{cid}/contact")
	public String showContactDetails(@PathVariable("cid")Integer id,Model m,Principal principal) {
		Optional<Contact> contactOpt=this.contactRepository.findById(id);
		Contact contact=contactOpt.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
		m.addAttribute("contact", contact);
		}
		System.out.println(id);
		return "normal/contact_details";
		
	}
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid")Integer cid, Model m, HttpSession session, Principal principal) {
	Contact contact=	this.contactRepository.findById(cid).get();
	
	User user=this.userRepository.getUserByUserName(principal.getName());
	this.contactRepository.delete(contact);
	user.getContacts().remove(contact);
	System.out.println("Deleted successfully");
	session.setAttribute("message", new Message("Contact deleted successfully","success"));
		
	return "redirect:/user/show_contacts/0";
		}
	@PostMapping("/update-contact/{cid}")
	public String updateForm(Model m, @PathVariable("cid") Integer cid) {
		m.addAttribute("title", "Update Form");
		Contact contact=this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
		
		return "normal/update_form";
		
	}
	@PostMapping("/updatecontact")
		public String updateHandller(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, HttpSession session, Principal p) {
			try {
				Contact oldcontactDetail=this.contactRepository.findById(contact.getCid()).get();
				if(!file.isEmpty()) {
					File deleteFile=new ClassPathResource("static/img").getFile();
					File file1=new File(deleteFile,oldcontactDetail.getImage());
					file1.delete();
					
					
					File saveFile=new ClassPathResource("static/img").getFile();
					Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					contact.setImage(file.getOriginalFilename());
					
				}else {
					contact.setImage(oldcontactDetail.getImage());
				}
				User user=this.userRepository.getUserByUserName(p.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
				session.setAttribute("messsage", new Message("Your contact is updated", "success"));
			}catch(Exception e) {
				e.printStackTrace();
			}
		return  "redirect:/user/"+contact.getCid()+"/contact";
			
		}
	
	}


