package com.prettyshopbe.prettyshopbe.controller;

import com.prettyshopbe.prettyshopbe.dto.PasswordDto;
import com.prettyshopbe.prettyshopbe.dto.ResponseDto;
import com.prettyshopbe.prettyshopbe.dto.user.SignInDto;
import com.prettyshopbe.prettyshopbe.dto.user.SignInResponseDto;
import com.prettyshopbe.prettyshopbe.dto.user.SignupDto;
import com.prettyshopbe.prettyshopbe.exceptions.AuthenticationFailException;
import com.prettyshopbe.prettyshopbe.exceptions.CustomException;
import com.prettyshopbe.prettyshopbe.model.User;
import com.prettyshopbe.prettyshopbe.respository.UserRepository;
import com.prettyshopbe.prettyshopbe.service.AuthenticationService;
import com.prettyshopbe.prettyshopbe.service.UserService;
import com.prettyshopbe.prettyshopbe.until.Helper;
import jakarta.xml.bind.DatatypeConverter;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.prettyshopbe.prettyshopbe.enums.ResponseStatus;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress; // <- Thêm dòng này vào
import javax.mail.internet.MimeMessage;

@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(@PathVariable Integer id, @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token);
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/all")
    public List<User> findAllUser(@RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token);
        return userRepository.findAll();
    }

    @GetMapping("/getrole")
    public ResponseEntity<String> getRole(@RequestParam("token") String token) throws AuthenticationFailException {
        // Code để xác thực token và lấy thông tin user từ cơ sở dữ liệu
        User user = authenticationService.getUser(token);

        // Lấy role của user và trả về kết quả dưới dạng chuỗi JSON
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("role", user.getRole().toString());
        return new ResponseEntity<>(jsonResult.toString(), HttpStatus.OK);
    }


    @PostMapping("/signup")
    public ResponseDto Signup(@RequestBody SignupDto signupDto) throws CustomException {
        return userService.signUp(signupDto);
    }

    //TODO token should be updated
    @PostMapping("/signIn")
    public SignInResponseDto Signup(@RequestBody SignInDto signInDto) throws CustomException {
        return userService.signIn(signInDto);
    }

    @PutMapping("/updateuseruser")
    public ResponseEntity<User> updateUserForUSer(@RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token);
        User user = authenticationService.getUser(token);

        if (!userRepository.existsById(user.getId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User updatedUser = userRepository.save(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/updateuseradmin/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id,
                                           @RequestBody User newUser,
                                           @RequestParam("token") String token) throws AuthenticationFailException {

        authenticationService.authenticate(token);
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User oldUser = userOptional.get();
        // Cập nhật các thuộc tính mới của đối tượng newUser
        if (newUser.getFirstName() != null) {
            oldUser.setFirstName(newUser.getFirstName());
        }
        if (newUser.getLastName() != null) {
            oldUser.setLastName(newUser.getLastName());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getPassword() != null) {
            oldUser.setPassword(newUser.getPassword());
        }
        if (newUser.getRole() != null) {
            oldUser.setRole(newUser.getRole());
        }
        userRepository.save(oldUser);

        return new ResponseEntity<>(oldUser, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Integer id, @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token);
        if (!userRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/resetpassword")
    public ResponseDto resetPassword(@RequestParam("email") String email) throws CustomException {
        User user = userRepository.findByEmail(email);
        if (!Helper.notNull(user)) {
            throw new CustomException("User not found");
        }
        try {
            // generate new password and send to user
            String newPassword = newPassword();
            user.setPassword(hashPassword(newPassword));
            userRepository.save(user);

            // send new password to user's email
            String messageContent = "Your new password is: " + newPassword;
            System.out.println(user.getEmail());
            sendEmail(user.getEmail(), "New Password", messageContent);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new ResponseDto(ResponseStatus.ERROR.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDto(ResponseStatus.ERROR.toString());
        }
        return new ResponseDto(ResponseStatus.SUCCESS.toString());
    }

    private void sendEmail(String recipientEmail, String subject, String messageContent) throws CustomException, MessagingException {
        // Setup mail server
        String mailServerHost = "smtp.gmail.com";
        int mailServerPort = 587;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailServerHost);
        props.put("mail.smtp.port", mailServerPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");


        String username = "19521317@gm.uit.edu.vn"; // replace with your email address
        String password = "raplacepassword"; // replace with your email password

        // Authenticate the user
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // Create a session
        Session session = Session.getInstance(props, auth);

        System.out.println(recipientEmail);
        // Create a message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setText(messageContent);

        // Send the message
        Transport.send(message);
    }

    private String newPassword() {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    @GetMapping("/getprofile")
    public ResponseEntity<User> getProfile(@RequestParam("token") String token) throws AuthenticationFailException {
        User user = authenticationService.getUser(token);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/editprofile")
    public ResponseEntity<User> changeProfile(
            @RequestBody User newUser,
            @RequestParam("token") String token) throws AuthenticationFailException {


        User oldUser = authenticationService.getUser(token);

        // Cập nhật các thuộc tính mới của đối tượng newUser
        if (newUser.getFirstName() != null) {
            oldUser.setFirstName(newUser.getFirstName());
        }
        if (newUser.getLastName() != null) {
            oldUser.setLastName(newUser.getLastName());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getPassword() != null) {
            oldUser.setPassword(newUser.getPassword());
        }
        if (newUser.getRole() != null) {
            oldUser.setRole(newUser.getRole());
        }
        userRepository.save(oldUser);

        return new ResponseEntity<>(oldUser, HttpStatus.OK);
    }

    Logger logger = (Logger) LoggerFactory.getLogger(UserService.class);

    @PostMapping("/changepassword")
    public ResponseDto changePassword(@RequestParam("token") String token, @RequestBody PasswordDto passwordDto) throws CustomException, NoSuchAlgorithmException {
        User currentUser = authenticationService.getUser(token); // Get currently authenticated user
        String encryptedPassword = "";
        System.out.println(passwordDto.getPassword());
        try {
            encryptedPassword = hashPassword(passwordDto.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("hashing password failed {}", e.getMessage());
        }


        currentUser.setPassword(encryptedPassword); // Hash and set the user's new password
        userRepository.save(currentUser); // Save the updated user to the database
        return new ResponseDto(ResponseStatus.SUCCESS.toString());
    }

    String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }
}
