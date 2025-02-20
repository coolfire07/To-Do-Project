import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Task } from '../task.model';
import { TaskService } from '../task.service';
import {filter} from 'rxjs';

@Component({
  selector: 'app-task-list',
  standalone: false,
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css'] // Исправлено на styleUrls
})
export class TaskListComponent implements OnInit {
  @Input() tasks: Task[] = [];
  filters = { status: '', date: '', keyword: '', sort: '' };
  selectedTask: Task | null = null;

  @Output() editTaskEvent = new EventEmitter<number>();
  @Output() deleteTaskEvent = new EventEmitter<number>();
  @Output() taskEdited = new EventEmitter<Task>();
  @Output() taskDeleted = new EventEmitter<number>();

  constructor(private taskService: TaskService) {}

  ngOnInit() {
    this.fetchTasks();
  }

  fetchTasks() {
    console.log('Current filters before request:', this.filters);
    if (this.filters.date) {
      const dateObj = new Date(this.filters.date);
      if (!isNaN(dateObj.getTime())) {
        dateObj.setHours(12, 0, 0, 0);
        this.filters.date = dateObj.toISOString().split('T')[0];
      }
    }


    console.log('filters после обработки:', this.filters)
    this.taskService.getTasks(this.filters).subscribe(
      (data) => {
        this.tasks = data;
      },
      (error) => {
        console.error(error);
        alert('error fetching tasks');
      }
    );
    console.log(this.filters);
  }

  editTask(id: number) {
    this.taskService.getTask(id).subscribe(
      (task) => {
        this.selectedTask = task;
        this.taskEdited.emit(task);
        this.editTaskEvent.emit(id);
      },
      (error) => {
        console.error(error);
        alert('error fetching task');
      }
    );
  }

  deleteTask(id: number) {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id).subscribe(
        () => {
          this.fetchTasks();
          this.taskDeleted.emit(id);
        },
        (error) => {
          console.error(error);
          alert('error deleting task');
        }
      );
    }
  }

  onTaskSaved(task: Task) {
    if (task.id) {
      this.taskService.updateTask(task.id, task).subscribe(
        () => {
          this.fetchTasks();
          this.selectedTask = null;
        },
        (error) => {
          console.error(error);
          alert('error updating task');
        }
      );
    } else {
      this.taskService.addTask(task).subscribe(
        () => {
          this.fetchTasks();
          this.selectedTask = null;
        },
        (error) => {
          console.error(error);
          alert('error adding task');
        }
      );
    }

    this.selectedTask = null;
  }

  resetFilters() {
    this.filters = { status: '', date: '', keyword: '', sort: '' };
    this.fetchTasks();
  }


  protected readonly filter = filter;
}
