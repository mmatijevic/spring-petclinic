/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

        private final VetRepository vetRepository;

        private static final String VIEWS_VET_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	public VetController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

        @GetMapping({ "/vets" })
        public @ResponseBody Vets showResourcesVetList() {
                // Here we are returning an object of type 'Vets' rather than a collection of Vet
                // objects so it is simpler for JSon/Object mapping
                Vets vets = new Vets();
                vets.getVetList().addAll(this.vetRepository.findAll());
                return vets;
        }

        @GetMapping("/vets/{vetId}/edit")
        public String initUpdateVetForm(@PathVariable("vetId") int vetId, Model model) {
                Vet vet = this.vetRepository.findById(vetId)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid vet Id:" + vetId));
                model.addAttribute(vet);
                return VIEWS_VET_CREATE_OR_UPDATE_FORM;
        }

        @PostMapping("/vets/{vetId}/edit")
        public String processUpdateVetForm(@Valid Vet vet, BindingResult result,
                        @PathVariable("vetId") int vetId, RedirectAttributes redirectAttributes) {
                if (result.hasErrors()) {
                        return VIEWS_VET_CREATE_OR_UPDATE_FORM;
                }
                vet.setId(vetId);
                this.vetRepository.save(vet);
                redirectAttributes.addFlashAttribute("message", "Vet information updated");
                return "redirect:/vets.html";
        }

}
