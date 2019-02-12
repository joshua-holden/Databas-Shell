****************
* To-do List Manager Project 
* CS410
* 04-29-2018
* Joshua Holden
**************** 

OVERVIEW:

 Simple to-do list manager shell.

COMPILING AND RUNNING:
 
 $ java -jar todolist.jar 'mysql://msandbox:<password>@localhost:<PORT>/todolist?useLegacyDatetimeCode=false&serverTimezone=UTC'


PROGRAM DESIGN AND DESCRIPTION:

 For the ER diagram, I designed the database as a single 'task' entity with the following attributes: id, label, date created, due date, isCompleted, isCancelled and tag (multivalued). Translating this to a DLL, I created a task table and a tag table. The tag table uses the primary key of the task for its primary key to associate the two. I chose to implement the isCompleted and isCancelled attributes as booleans. For the TodoShell, I used the CharityShell as a guide as I implemented the methods. For any method that inserted or updated, I made sure to use transactions, specifically setting auto-commit to false, then committing if successful or rolling back the query if there was an exception. I prepared statement for the query strings for all of the commands, but did not use transactions with commands only using select statements.

For the test values, the first 15 tasks (id 1-15) are active and (depending on when graded), will be best to test the , 'active', 'active tag'  'overdue', 'due today', and 'due soon' commands. The tags are 'school' and 'work' and the 'completed tag' command should return tasks with id 21-25. The tasks have labels for every letter of the alphabet ('task a' through 'task z') and the 'search keyword' command can be tested accordingly.
