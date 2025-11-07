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
import org.springframework.samples.petclinic.system.FormBindingHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private static final String VIEWS_VET_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	private final VetRepository vetRepository;

	private final SpecialtyRepository specialtyRepository;

	public VetController(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
		this.vetRepository = vetRepository;
		this.specialtyRepository = specialtyRepository;
	}

	@ModelAttribute("specialties")
	public List<Specialty> populateSpecialties() {
		return (List<Specialty>) this.specialtyRepository.findAll();
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
			.orElseThrow(() -> new IllegalArgumentException(
					"Vet not found with id: " + vetId + ". Please ensure the ID is correct."));
		model.addAttribute("vet", vet);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/vets/{vetId}/edit")
	public String processUpdateVetForm(@Valid Vet vet, BindingResult result, @PathVariable("vetId") int vetId,
			@RequestParam(value = "specialtyIds", required = false) List<Integer> specialtyIds,
			RedirectAttributes redirectAttributes, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("vet", vet);
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		}

		if (vet.getId() != null && vet.getId() != vetId) {
			result.rejectValue("id", "mismatch", "The vet ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Vet ID mismatch. Please try again.");
			return "redirect:/vets/{vetId}/edit";
		}

		// Update vet with the form data
		vet.setId(vetId);

		// Update specialties using the shared helper
		List<Specialty> allSpecialties = (List<Specialty>) this.specialtyRepository.findAll();
		FormBindingHelper.bindCollectionFromIds(
			specialtyIds,
			allSpecialties,
			vet::clearSpecialties,
			vet::addSpecialty,
			Specialty::getId
		);

		this.vetRepository.save(vet);
		redirectAttributes.addFlashAttribute("message", "Vet Values Updated");
		return "redirect:/vets.html";
	}

}
