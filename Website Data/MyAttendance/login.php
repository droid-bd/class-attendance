<?php
session_start();
// store session data
//$_SESSION['teacher_id'] = "null";
?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

</head>
<body style="background: #C1EA4E">

<div id="templatemo_wrapper">

	<div id="templatemo_menu" style="">
           
    </div> <!-- end of templatemo_menu -->

    <div id="templatemo_left_column">
    
    
    </div> <!-- end of templatemo_left_column -->
    
    <div id="">
    
        <div id="templatemo_main" style="background: #007ACC; padding: 100px;">
        
        <?php 
            //if(!isset($_SESSION['teacher_id']))
            if ($_SERVER['REQUEST_METHOD'] == 'GET')
            {
        ?>    
            <center>
                Please Login
            <form action="<?php echo $_SERVER['PHP_SELF'] ?>" method="POST">
                <table title="Please Log in" >
                    <tr>
                        <td>Name:</td>
                        <td><input type=text name="teacher_id" placeholder="Teacher ID" required="netcse" value=""/></td>
                    </tr>
                    <tr>
                        <td>Password:</td>
                        <td><input type=password name="password" placeholder="Password" required="netcse" value="" /></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input type="submit" value="Log in" /></td>
                    </tr>
                </table>
            </form>
            </center>
        
        <?php 
        }
            if ($_SERVER['REQUEST_METHOD'] == 'POST') 
            {
                //echo "post method";      
                require_once __DIR__.'/db_connect.php';
                $db = new DB_CONNECT();
                
                 $sql = "SELECT Teacher_Id, password FROM attendance WHERE Teacher_Id='". $_POST['teacher_id'] ."' AND password= '" . $_POST['password'] . "'";
                 $result = mysql_query($sql);

                 $flag = 0;
                 
                $response = array();

                /*  */
                if(!empty($result))
                {
                    if(mysql_num_rows($result)>0)
                    {
                         while($row = mysql_fetch_array($result))
                         {
                             if($row['Teacher_Id'] == $_POST['teacher_id'])
                             {
                                 if($row['password'] == $_POST['password'])
                                 {
                                     $flag = 1;
                                 }
                             }
                         }
                    }

                }
                else echo "No data saved for you. Please try again.";
                
                if($flag==1)
                {
                    $t_id = $_POST['teacher_id'];

                    $sql = "SELECT * FROM attendance WHERE Teacher_Id='". $t_id ."' ORDER BY Date";
                    $result = mysql_query($sql);

                    $response = array();

                    if(!empty($result))
                    {
                        if(mysql_num_rows($result)>0)
                        {
                            echo "<center><a href=\"login.php\" class=\"current\">Back</a>";
                            
                            $response['teacher'] = array();
                            ?> <div style="background: #007ACC"> 

                                <h2>Attendance info</h2> 
                                <?php

                                $counter = 1;
                                        
                            while($row = mysql_fetch_array($result))
                            {
                                $tcr = array();
                                if($counter == 1)
                                {
                                    echo "<h4>Teacher ID: " . $row['Teacher_Id'] . "</h4>";
                                    echo "<table cellspacing=\"0\" cellpadding=\"0\" border=\"1\" width=\"60%\">";
                                    echo "<th>Student ID: </th><th>Section: </th><th>Course No.: </th><th>Dept.: </th><th>Date: </th>";
                                    $counter++;
                                }
                                $tcr['s_id'] = $row['Student_Id'];
                                $tcr['g_id'] = $row['GROUPD'];
                                $tcr['c_no'] = $row['Course_No'];
                                $tcr['d_id'] = $row['Department_Id'];
                                $tcr['date'] = $row['Date'];

                                echo  "<tr><td>" . $tcr['s_id']. "</td><td>" . $tcr['g_id']. "</td><td>" .  $tcr['c_no']. "</td><td>" . $tcr['d_id']. "</td><td>" . $tcr['date']. "</td></tr>";

                                echo "</center>";
                                array_push($response['teacher'], $tcr);                
                            }
                            $response['success'] = 1;
                            //echo json_encode($response);

                            echo "</table>";
                        }
                        else echo "<center>Sorry sir! There is no information for you. <a href=\"login.php\" class=\"current\">Back</a></center>";
                    }
                    else 
                    {
                        $response['success'] = 0;
                        echo json_encode($response);
                    }
                }
                else echo "<center>No data found for you. Please <a href=\"login.php\" class=\"current\">try again</a></center>";
                
            }
            
            /*
            
            */
            ?>
            <center><h6>&COPY;All rights reserved by Md. Milon Islam and Md. Kamrul Hasan netcse.</h6></center>
                            </div>
            
    
  <div class="cleaner"></div>
    <!-- end of templatemo_main -->
  <div class="cleaner_h20"></div>
  
    <div class="cleaner"></div>
    </div>
</div> <!-- end of warpper -->
</body>
</html>