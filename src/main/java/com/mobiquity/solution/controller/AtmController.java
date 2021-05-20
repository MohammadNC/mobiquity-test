package com.mobiquity.solution.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobiquity.solution.exception.CityNotFoundException;
import com.mobiquity.solution.model.ATM;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locator")
public class AtmController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/atms", method = RequestMethod.GET)
    public ResponseEntity<List<ATM>> getAllAtms () {
    	List<ATM> result = null;

        try {
        	result = getFormatedAtmsList();
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Exception occurred while processing the request..");
		}
        
        return new ResponseEntity<List<ATM>>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/atms/{city_name}", method = RequestMethod.GET)
    public ResponseEntity<List<ATM>> getAtmsByCity (final HttpServletRequest request,
                                    @PathVariable(value = "city_name") String cityName) {
  	 
    	List<ATM> result = null;
		try {
			result = getAtmsByCity(cityName);
			if (null == result || result.size() == 0) {
				throw new CityNotFoundException("No Atm's are found for the given city:"+cityName);
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Exception occurred while processing the request..");
		}

        return new ResponseEntity<List<ATM>>(result, HttpStatus.OK);
    }
    
    /**
     * To get the list of all ATM's by invoking api.
     * @return
     */
    private String getAllAtmDetails () {
        ResponseEntity<String> response = restTemplate.exchange("https://www.ing.nl/api/locator/atms/",
                HttpMethod.GET,
                null,
                String.class);

        return response.getBody().split("\\n")[1];
    }
    
    
    /**
     * Getting the formated ATM's List.
     * @return
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    private List<ATM> getFormatedAtmsList() throws JsonMappingException, JsonProcessingException {
    	ObjectMapper mapper = new ObjectMapper();
    	List<ATM> result = new ArrayList<>();
    	result = Arrays.asList(mapper.readValue(getAllAtmDetails(), ATM[].class));    	
    	return result;
    }
    
    /**
     * To Get all the ATM's by city.
     * @param city
     * @return
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    private List<ATM> getAtmsByCity (String city) throws JsonMappingException, JsonProcessingException {
    	List<ATM> atmsList = getFormatedAtmsList();
    	List<ATM> result = atmsList.parallelStream()
    					.filter(atm -> atm.getAddress().getCity().equalsIgnoreCase(city))
    					.collect(Collectors.toList());
      	return result;
    }

}
