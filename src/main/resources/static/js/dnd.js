document.addEventListener('DOMContentLoaded', function () {
    const todos = document.querySelectorAll('.todo');
    todos.forEach(t => {
        t.addEventListener('dragstart', ev => {
            ev.dataTransfer.setData('text/plain', ev.target.id);
        });
    });

    const cols = document.querySelectorAll('.column');
    cols.forEach(col => {
        col.addEventListener('dragover', ev => ev.preventDefault());
        col.addEventListener('drop', ev => {
            ev.preventDefault();
            const id = ev.dataTransfer.getData('text/plain');
            const el = document.getElementById(id);
            if (!el) return;
            col.appendChild(el);
            // determine status from column id
            let status = 'TODO';
            if (col.id.includes('doing')) status = 'DOING';
            if (col.id.includes('completed')) status = 'COMPLETED';
            fetch('/api/todos/' + id + '/status', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: status })
            }).then(res => {
                if (!res.ok) {
                    alert('Status update failed');
                    // naive revert: move back to TODO
                }
            });
        });
    });
});
