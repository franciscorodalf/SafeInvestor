package es.franciscorodalf.safeinvestor.bancos.web;

import es.franciscorodalf.safeinvestor.bancos.client.GoCardlessClient;
import es.franciscorodalf.safeinvestor.bancos.service.BancoService;
import es.franciscorodalf.safeinvestor.config.Toasts;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/bancos")
public class BancoWebController {

    private final BancoService bancoService;
    private final GoCardlessClient client;
    private final CurrentUser currentUser;
    private final MessageSource messages;

    public BancoWebController(BancoService bancoService,
                              GoCardlessClient client,
                              CurrentUser currentUser,
                              MessageSource messages) {
        this.bancoService = bancoService;
        this.client = client;
        this.currentUser = currentUser;
        this.messages = messages;
    }

    private String t(String key, Object... args) {
        return messages.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /** Listado de conexiones actuales del usuario. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("conexiones", bancoService.findAll(currentUser.get()));
        model.addAttribute("configured", client.isConfigured());
        return "bancos/list";
    }

    /** Selector de banco para crear una conexión nueva. */
    @GetMapping("/conectar")
    public String connectPage(@RequestParam(required = false) String country, Model model) {
        if (!client.isConfigured()) {
            return "redirect:/bancos";
        }
        String defaultCountry = client.getProperties().defaultCountry();
        model.addAttribute("country", country != null ? country : defaultCountry);
        model.addAttribute("institutions", bancoService.listInstitutions(country));
        return "bancos/conectar";
    }

    /** Inicia el proceso: crea el requisition y redirige al banco. */
    @PostMapping("/conectar")
    public String startLink(@RequestParam String institutionId,
                            @RequestParam String institutionName,
                            @RequestParam(required = false) String institutionLogo,
                            RedirectAttributes ra) {
        if (!client.isConfigured()) {
            Toasts.error(ra, t("bancos.not_configured"));
            return "redirect:/bancos";
        }
        try {
            String bankLink = bancoService.startLink(currentUser.get(), institutionId,
                    institutionName, institutionLogo);
            return "redirect:" + bankLink;
        } catch (Exception e) {
            Toasts.error(ra, t("bancos.link_error", e.getMessage()));
            return "redirect:/bancos";
        }
    }

    /** Vuelta desde el banco: GoCardless añade ?ref= con el {@code reference} que enviamos. */
    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String ref,
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String details,
                           RedirectAttributes ra) {
        if (error != null) {
            Toasts.error(ra, t("bancos.callback_error", error + " " + (details != null ? details : "")));
            return "redirect:/bancos";
        }
        if (ref == null) {
            Toasts.error(ra, t("bancos.callback_missing_ref"));
            return "redirect:/bancos";
        }
        // Nuestro reference tiene el formato: safeinvestor-{userId}-{uuid}
        // GoCardless nos devuelve el requisition ID por otra vía, no en el ref. En el flujo
        // simple guardamos el requisitionId al crear, así que buscamos por él directamente.
        // El parámetro `ref` que enviamos coincide con `reference` del requisition, no con
        // su ID. Usamos ese ref para buscar la conexión pendiente más reciente del usuario
        // y llamar a finishLink con su requisitionId.
        try {
            var user = currentUser.get();
            var pending = bancoService.findAll(user).stream()
                    .filter(c -> c.getStatus().name().equals("PENDING"))
                    .findFirst();
            if (pending.isEmpty()) {
                Toasts.info(ra, t("bancos.callback_no_pending"));
                return "redirect:/bancos";
            }
            var conn = bancoService.finishLink(user, pending.get().getRequisitionId());
            if (conn.getStatus() == es.franciscorodalf.safeinvestor.bancos.domain.BankConnection.Status.LINKED) {
                Toasts.success(ra, t("bancos.link_success", conn.getInstitutionName()));
            } else {
                Toasts.info(ra, t("bancos.link_pending"));
            }
        } catch (Exception e) {
            Toasts.error(ra, t("bancos.link_error", e.getMessage()));
        }
        return "redirect:/bancos";
    }

    @PostMapping("/{id}/sync")
    public String sync(@PathVariable Long id, RedirectAttributes ra) {
        try {
            BancoService.SyncResult r = bancoService.sync(currentUser.get(), id);
            Toasts.success(ra, t("bancos.sync_ok", r.created(), r.skipped(), r.errored()));
        } catch (Exception e) {
            Toasts.error(ra, t("bancos.sync_error", e.getMessage()));
        }
        return "redirect:/bancos";
    }

    @PostMapping("/{id}/borrar")
    public String revoke(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bancoService.revoke(currentUser.get(), id);
            Toasts.success(ra, t("bancos.revoke_ok"));
        } catch (Exception e) {
            Toasts.error(ra, t("bancos.revoke_error", e.getMessage()));
        }
        return "redirect:/bancos";
    }
}
