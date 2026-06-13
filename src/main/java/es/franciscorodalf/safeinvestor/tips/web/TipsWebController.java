package es.franciscorodalf.safeinvestor.tips.web;

import es.franciscorodalf.safeinvestor.tips.Tip;
import es.franciscorodalf.safeinvestor.tips.TipsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/tips")
public class TipsWebController {

    private final TipsService tipsService;

    public TipsWebController(TipsService tipsService) {
        this.tipsService = tipsService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String categoria, Model model) {
        List<Tip> tips = tipsService.byCategoria(categoria);
        model.addAttribute("tips", tips);
        model.addAttribute("categorias", tipsService.categorias());
        model.addAttribute("currentCategoria", categoria);
        model.addAttribute("total", tipsService.total());
        model.addAttribute("tipsService", tipsService); // para iconoDe/etiquetaDe en la plantilla
        return "tips/list";
    }
}
