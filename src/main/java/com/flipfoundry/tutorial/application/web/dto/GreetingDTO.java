package com.flipfoundry.tutorial.application.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * <p>The Greeting DTO containing the following:</P>
 * <ul>
 *  <li>An Id
 *  <li>A content field
 * </ul>
 *
 * @deprecated As of release 1.1, Moved to {@link #(GreetingDTOV2) }
 * @author  <a href="mailto:jim.dellostritto@gmail.comm">Jim DelloStritto</a>
 * @version 1.1
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated(since = "1.1", forRemoval = true)
public class GreetingDTO {

    /**
     * @since 1.0
     */
    private long id;
    /**
     * @since 1.0
     */
    private String content;

}
