package com.medicine.query.controller;


import com.medicine.query.exception.MedException;
import com.medicine.query.model.IbonRsp;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;

@Slf4j
@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    MedicineService medicineService;

    @RequestMapping(value = {"/login", "/index"}, method = RequestMethod.GET)
    public String login(ModelMap model) {
        return "login";
    }

    @RequestMapping(value = {"/home", "/", "/index"}, method = RequestMethod.GET)
    public String home(ModelMap model) {
        return "home";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String POSTQuery(@RequestParam String querystring, Model model) {
        try {
            model.addAttribute("meds", medicineService.getMedicine(querystring));
        } catch (Exception e) {
            throw new MedException(e);
        }
        model.addAttribute("querystring", querystring);
        return "result";
    }

    @RequestMapping(value = "/pdfreport", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> medsReport(@RequestParam String querystring) throws Exception {

        ByteArrayInputStream bis = medicineService.medsReport(medicineService.getMedicine(querystring));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + querystring + ".pdf");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @RequestMapping(value = "/qrcode", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public String createQRprint(@RequestParam String querystring, Model model) throws Exception {
        //IbonRsp rsp = medicineService.createQRcode(medicineService.getMedicine(querystring));
        model.addAttribute("fileCode", "ivborw0kggoaaaansuheugaaajyaaacwcaiaaaczy+a1aaaaaxnsr0iars4c6qaaaarnqu1baacxjwv8yquaaaajcehzcwaadsmaaa7dacdvqgqaaawzsurbvhhe7zvtuhwxdatz/i9ncguv7qhlz62r92xo/il5y1oj20vy582fzrx4s/n6xf6bclcvahgkchsftt+alhth9gpsvwfdkmltfdh+a7pqhnsrsp0gdkeit1dg+w3oqhfur8d2g9cfitxege03oatful0c229af4pweww234aufohb25+rfs7f/rkkl8hna0tbjdttkz8gfzzucql9rjuigoib9yvcbwv0yb0ggqtsfxyw2ehvzwmjftbpoy6+sznvjynuyc0o2sda0ebe/s4cnhgkdg0y9xmrgnveccpdgkmujl/niq+g6ddiuhbpqwulcqgihjxj+lmymooyp17jvwk05kfit07ufbs0aptys0zg91urnj0qdof9fdhiz87gezzfhkblyfvg7tqzj4iwbso9vyaxf2+eyqgwpo3nhq01ohu7uiqf7/ia+fej+rkarf1cf4knvqjhy71mjxvpgw7i0i0xtn+ospfofcf4vsgxujemi6ncxqi+7ijwuigfcxbasfgjr3gjfeeshpvxx/fh5bkfoz+e4y9dj+l8z53esjnvvkrkv81b00qen16ocdee1ouhqs+tuhcm2oklujinevkle13g7/5gij6komakrwijrsojbikoet4/cxni6ntnjagirfi6/yla2crpw7qubfpas3dwn9jab8+z0dgdgvo1xtlcw1l45xlv50juupcrdlsbv4tnhw1qgjiseyowqz8sf/vgfjyt6+rsbheig+erru/obzj8i01ya/bfenot0amirjaub6nmgmwhwjovlcncsz8joaaedg8mfuezqvqzlolriggyecqdt/3vltf/nzm3b8qsozos3hnbfyhwkghbmww6prv6ozp4ff4rivbtgczqmnhik+6fhg+j7rjl0bzjrimtque6i8j7tokag8y+tlflo8lcdaclc/114a2kki420vqxhwtzexfon48qaye6bukvrxjn7jf1ecteytaq4ws6rkx5ucni1z0qdohff1rdefbyivfoc5ordshoyonqpwehujdev+f3ksm86dhhwadk7kl7g6ki70mi8egbwpgoxizrvzfebqxxnqgld77oodbbgcxcoads/dyjakjkortkjrti/cwu9sgk9gv99dkwh37f30ihpu0ieuhh7bpdznemiuirflzey5tnhqpzwub6ypd+jdnxozofjxnlo1x0m0zalmrnbqxhahahwpaei/d4m0vyav/seyoov4qi/cyypbryou8dwuz6cj357wdhy4mhczrqyqhs48ilfmcbsaypr+vj0en3qxhenvgt+vsbaw0dxwe1cafufogbp3xhqowmhxgwjug03ywnh1cjozkhzmvfztdc6gmmcjsffsj8qdkkr6pd0zpj3ndot08nd9hxmcyejcirqdzsgyn8z7wor6gpbigyxumskzpceqv+i9nyhfoogvrnqbogqaawer7ijckzwyvsb3uhfiysk4yc5a56urfqwtddz5icb8yd11d+vgsbmqyk9r7w+a0bzftd8manviqfjzuxqum3fproekeip2sa1fgva6rincsfisnrd120ygt7g7ntz8lbdrygjs26cv60jgtewq4i03dho/tf+cbmty6rz4onfgilolijg17+wgcazfhlbbqwowb06ud4kbhmjyzi/q/upwsrfrqswunlx438frbckasluisfcsxrhsnintvzsg+a//azsfy7znup0odcrqu4tk0bedz0wyhlhavbqc1fhdxdfsos4uiwaksryu23hbjeubcm2ljewnntoswrlgsjthqr1nrbkeuec8goluwend0wyhlhqjbqsxfhtbefsks4eizaukry022hlbeubko2fbhwdfsos4qlwagtryq13rbkeufcmgplewfnt4wy/glrlpekx3/fpwaaaabjru5erkjggg==");
        model.addAttribute("fileNumber", "5566");
        model.addAttribute("querystring", querystring);
        return "qrcode";
    }

}
