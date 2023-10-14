package com.portfolio.blogifyx.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.blogifyx.models.ERole;
import com.portfolio.blogifyx.models.Role;
import com.portfolio.blogifyx.models.User;
import com.portfolio.blogifyx.payload.request.LoginRequest;
import com.portfolio.blogifyx.payload.request.SignupRequest;
import com.portfolio.blogifyx.payload.response.JwtResponse;
import com.portfolio.blogifyx.payload.response.MessageResponse;
import com.portfolio.blogifyx.repository.RoleRepository;
import com.portfolio.blogifyx.repository.UserRepository;
import com.portfolio.blogifyx.security.jwt.JwtUtils;
import com.portfolio.blogifyx.security.services.UserDetailsImpl;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
		//Authenticate {username, password}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		
		//Update securityContext using authentication object
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		//Generate jwt token
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		//Get userDetails from authentication object
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority()).collect(Collectors.toList());
		
		return ResponseEntity.ok(new JwtResponse(
				jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles));
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerNewUser(@Valid @RequestBody SignupRequest signupRequest){
		//Check if username or email is already exists
		if(userRepository.existsByUsername(signupRequest.getUsername()))
			return ResponseEntity.badRequest().body(new MessageResponse("ERROR: Username is already taken"));
		if(userRepository.existsByEmail(signupRequest.getEmail()))
			return ResponseEntity.badRequest().body(new MessageResponse("ERROR: Email is already taken"));
		
		//Create new user with USER role if not specified
		User user = new User(
				signupRequest.getUsername(),
				signupRequest.getEmail(),
				passwordEncoder.encode(signupRequest.getPassword())
		);
		
		Set<String> strRoles = signupRequest.getRoles();
		Set<Role> roles = new HashSet<>();
		if(strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(()-> new RuntimeException("ERROR: Role not found. Default role name is misspelled or not available."));
			roles.add(userRole);
		}else {
			strRoles.forEach(role -> {
				switch(role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
						.orElseThrow(()-> new RuntimeException("ERROR: Role not found. Default role name is misspelled or not available."));
					roles.add(adminRole);
					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
						.orElseThrow(()-> new RuntimeException("ERROR: Role not found. Default role name is misspelled or not available."));
					roles.add(modRole);
					break;
				default :
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
						.orElseThrow(()-> new RuntimeException("ERROR: Role not found. Default role name is misspelled or not available."));
					roles.add(userRole);
				break;
				}
				
			});
		}
		user.setRoles(roles);
		
		//Save the user to the database
		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponse("User Registred successfully."));
		
	}
	
	
	
	
}

