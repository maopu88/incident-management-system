
# Instructions to the candidate for the home task

Create a simple application related to incident management. The application will list all existing incidents, and users can add, delete and modify incidents. User login feature isn'ta necessity. You should complete this task independently and are free to use any resources or reference materials you deem appropriate. The completed work should be submitted via email or public GitHub no more than 48 hours after receiving this assignment. The exercise is intended to take 1 to 2 hours to complete. Upon review of the submission, afollow-up discussion will be arranged.

## Instructions

- Write in Java 17 and Spring Boot

- The primary entity is the incident

- All data should be held in memory; no persistence storage is necessary

- The main points to address are:

    Clear API

    Performance of all the main operations

    Thorough testing (includes unit testing and stress testing)

    Completeness of the application (if possible, including the creation of a frontend page using React)

    Use of containers, such as Docker, K8s

    Mechanism of caching

    Validation and Exception Handling

    Efficient data queries, including the use of database technology (spring data,SQL, pagination, indexing)

  Use of RESTful style

- The deliverable should be a self-contained project that we can easily run and test

- Use Maven

- lf you use external libraries outside of the standard JDK, please mention them inthe README and explain their purpose

- Ensure the page has the basic functionalities: adding/modifying/deleting incidents and displaying the incident list on the page

## APl to implement
Feel free to name your functions as you see fit, as long as the action is clearly stated.

- Create incident
- Delete incident
- Modify incident
- List all incidents
- If errors occur (for example, creating an existing incident or deleting a non-existentincident), appropriate eror handling should be in place, and error messages shouldbe communicated
- Unit testing the APl to ensure robustness and stability

## INTERNAL

- In case of necessity like-authorities etc., test the coresponding handling logic

- Ensure APl's performance to withstand possible slress tests.
