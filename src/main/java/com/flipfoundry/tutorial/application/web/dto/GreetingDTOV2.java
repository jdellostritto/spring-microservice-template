package com.flipfoundry.tutorial.application.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>The Greeting DTO V2 Containing:.</P>
 * <ul>
 *  <li>A content field
 * </ul>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.comm">Jim DelloStritto</a>
 * @version 1.0
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreetingDTOV2 {
     /**
      * @since 1.0
      */
     private String content;

}
