package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @Autowired
    private EntityManagerFactory emf;

    @RequestMapping("*")
    public String defaultMapping() {
        ArrayList<Signup> initial = new ArrayList<>();
        initial.add(new Signup("Party Man 123", "DaHoodz"));
        initial.add(new Signup("Secret_party", "Secret location"));
        signupRepository.save(initial);
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(@RequestParam String name, @RequestParam String address, Model model) {
        signupRepository.save(new Signup(name, address));
        return getSignups(model);
    }

    @RequestMapping(value = "/participants", method = RequestMethod.POST)
    @Transactional
    public String getAddress(@RequestParam String id, Model model) {
        EntityManager session = emf.createEntityManager();
        try {
            Query query = session.createQuery("SELECT address FROM Signup WHERE id = "+id);
            Object result = query.getResultList();
            model.addAttribute("address", result);
        }
        finally {
            if(session.isOpen()) session.close();
        }
        return getSignups(model);
    }

    @RequestMapping(value = "/participants", method = RequestMethod.GET)
    public String getSignups(Model model) {
        //remove secret party!
        model.addAttribute("signups",
                signupRepository.findAll().stream()
                        .filter(s -> !s.getName().equals("Secret_party"))
                        .collect(Collectors.toList()));
        return "participants";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String getAdminPanel(@RequestParam String admin, Model model) {
        if ("true".equals(admin)) {
            model.addAttribute("signups", signupRepository.findAll());
            return "admin";
        }

        return "redirect:/form/";
    }

}
