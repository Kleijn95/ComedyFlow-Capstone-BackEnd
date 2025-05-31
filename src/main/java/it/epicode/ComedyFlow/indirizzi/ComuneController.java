package it.epicode.ComedyFlow.indirizzi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comuni")
public class ComuneController {

    @Autowired
    private ComuneRepository comuneRepository;

    @GetMapping
    public List<ComuneResponse> getAll() {
        return comuneRepository.findAll().stream().map(comune -> {
            ComuneResponse res = new ComuneResponse();
            res.setId(comune.getId());
            res.setNome(comune.getNome());
            res.setProvinciaNome(comune.getProvincia().getNome());
            res.setProvinciaSigla(comune.getProvincia().getSigla());
            return res;
        }).toList();
    }
}
