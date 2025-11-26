package com.flipfoundry.tutorial.application.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>The Depart controller DTO.</P>
 * <ul>
 *  <li> A content field for a message.
 *  <li> A date field
 * </ul>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.1
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartDTO {
    /**
     * Constructor with content parameter only.
     * @param content The goodbye message content
     * @since 1.1
     */
    public DepartDTO(String content) {
        this.content = content;
    }

    /**
     * @since 1.0
     */
    private String content;

    /**
     * @since 1.1
     */
    private String date;

}
