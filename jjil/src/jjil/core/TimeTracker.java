/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jjil.core;

/**
 * TimeTracker facilitates the tracking of CPU time in order to find expensive
 * operations for optimizing. Since this is done differently on the various
 * platforms only the interface is provided here. The intention is that
 * a method with internal steps being monitored will be passed a TimeTracker
 * interface and use it to record time usage.
 * @author webb
 */
public interface TimeTracker {
    /**
     * At the end of the operation call endTask, passing the same name as used
     * in startTask. This stops the timer. It is not necessary that tasks be
     * properly nested.
     * @param szTaskName the name assigned to the task, matching the name used
     * in startTask
     */
    void endTask(String szTaskName);
    
    /**
     * Get a string for printing the cumulative times, for display to the user.
     * @return a string of the form task name1=time, task name=time, etc.
     */
    String getCumulativeTimes();
    
    /**
     * Reset the cumulative times to zero and clear all tasks.
     */
    void reset();

    /**
     * At the start of an expensive operation call startTask, passing a unique
     * name for the task. This starts the timer.
     * @param szTaskName the name assigned to the task
     */
    void startTask(String szTaskName);
    
}
