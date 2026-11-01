package com.shank.busbooking.service;

import com.shank.busbooking.dto.BusRequest;
import com.shank.busbooking.dto.BusResponse;
import com.shank.busbooking.entity.Bus;
import com.shank.busbooking.exception.ResourceNotFoundException;
import com.shank.busbooking.repository.BusRepository;
// [AI GENERATED] Removed unused import (Swagger's @Server is for API docs config, not services):
// import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusService {
    private final BusRepository busRepository;

    public BusService(BusRepository busRepository){
        this.busRepository = busRepository;
    }

    private BusResponse mapToResponse(Bus bus){
        BusResponse response = new BusResponse();

        response.setId(bus.getId());
        response.setBusName(bus.getBusName());
        response.setBusNumber(bus.getBusNumber());
        response.setSource(bus.getSource());
        response.setDestination(bus.getDestination());
        response.setTotalSeates(bus.getTotalSeats());
        response.setFare(bus.getFare());
        return response;
    }
    public BusResponse createBus(BusRequest request){
        Bus bus = new Bus();

        bus.setBusName(request.getBusName());
        bus.setBusNumber(request.getBusNumber());
        bus.setSource(request.getSource());
        bus.setDestination(request.getDestination());
        bus.setTotalSeats(request.getTotalSeates());
        bus.setFare(request.getFare());

        Bus savedBus = busRepository.save(bus);

        return mapToResponse(savedBus);
    }

    public List<BusResponse> getAllBuses(){
        return busRepository.findAll()
                .stream()
                .map(this :: mapToResponse)
                .toList();
    }

    public BusResponse getBusById(Long id){
        Bus bus = busRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Bus not found with Id: " + id) );
        return mapToResponse(bus);
    }

    public BusResponse updateBus(Long id,BusRequest request){
        Bus bus = busRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Bus not found with ID: " + id));

        bus.setBusName(request.getBusName());
        bus.setBusNumber(request.getBusNumber());
        bus.setSource(request.getSource());
        bus.setDestination(request.getDestination());
        bus.setTotalSeats(request.getTotalSeates());
        bus.setFare(request.getFare());

        Bus updatedBus = busRepository.save(bus);
        return mapToResponse(updatedBus);
    }

    public void deleteBus(Long id){
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bus notfound with ID: " +  id));
        busRepository.delete(bus);
    }
}
